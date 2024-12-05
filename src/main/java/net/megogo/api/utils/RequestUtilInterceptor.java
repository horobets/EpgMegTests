package net.megogo.api.utils;

import io.qameta.allure.Attachment;
import okhttp3.*;
import io.qameta.allure.Allure;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class RequestUtilInterceptor implements Interceptor {private static final Charset UTF8 = StandardCharsets.UTF_8;
    private final Logger logger = LoggerFactory.getLogger(RequestUtilInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {

        String threadName = Thread.currentThread().getName();
        StringBuffer allLogs = new StringBuffer();
        String attachmentName = "REQUEST: ";

        Request request = chain.request();

        allLogs.append("-------request-------");
        allLogs.append("\n[" + threadName + "]---> " + request.method() + " " + request.url());
        attachmentName += request.method() + " " + request.url().encodedPath();

        long startNs = System.nanoTime();
        String requestStartTime = LocalDateTime.now().toString();

        Response response = proceedRequest(chain, threadName, allLogs, attachmentName, request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        String requestFinishTime = LocalDateTime.now().toString();

        String requestAllure = getAllureViewRequest(threadName, allLogs, request, requestStartTime);
        String responseAllure = getAllureViewResponse(threadName, allLogs, response, requestFinishTime, tookMs);

        writeLog(allLogs, requestAllure, responseAllure);
        return response;
    }

    private String getAllureViewResponse(String threadName, StringBuffer allLogs, Response response, String requestFinishTime, long tookMs) throws IOException {
        String responseHeaders = getAndAppendResponseHeaders(threadName, allLogs, response);
        String responseBodyJson = getAndAppendResponseBody(threadName, allLogs, response, tookMs);
        String responseUrl = getResponseTimeLog(response, tookMs, requestFinishTime) + "\n\n";
        String responseBodyAllure = "Response:\n" + responseBodyJson + "\n\n";
        String responseHeadersAllure = "Headers:\n" + responseHeaders + "\n\n";
        return responseUrl + responseBodyAllure + responseHeadersAllure;
    }

    private String getAllureViewRequest(String threadName, StringBuffer allLogs, Request request, String requestStartTime) throws IOException {
        String requestHeaders = getAndAppendRequestHeaders(threadName, allLogs, request.headers(), request.body());
        String requestBodyJson = getAndAppendRequestBody(threadName, allLogs, request, request.body());
        String requestUrlAllure = "Time: " + requestStartTime + "\n" + "Url: " + request.url() + "\n\n";
        String requestBodyAllure = "Request body:\n" + requestBodyJson + "\n\n";
        String requestHeadersAllure = "Headers:\n" + requestHeaders + "\n\n";
        return requestBodyJson.length() < 5 ? requestUrlAllure + requestHeadersAllure : requestUrlAllure + requestBodyAllure + requestHeadersAllure;
    }

    private String getAndAppendRequestHeaders(String threadName, StringBuffer sb, Headers headers, RequestBody body) throws IOException {
        String requestHeaders = "";
        if (body != null) {
            if (body.contentType() != null) sb.append("\n[" + threadName + "]---> Content-Type: " + body.contentType());
            if (body.contentLength() != -1)
                sb.append("\n[" + threadName + "]---> Content-Length: " + body.contentLength());
        }

        for (int i = 0; i < headers.size(); i++) {
            sb.append("\n[" + threadName + "]---> " + headers.name(i) + ": " + headers.value(i));
            requestHeaders += headers.name(i) + ": " + headers.value(i) + "\n";
        }
        return requestHeaders;
    }

    private String getAndAppendRequestBody(String threadName, StringBuffer sb, Request request, RequestBody body) throws IOException {
        String requestBody = "";
        if (body != null) {
            Charset charset = UTF8;
            MediaType contentType = body.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            try (Buffer buffer = new Buffer()) {
                body.writeTo(buffer);
                if (isPlaintext(buffer)) {
                    requestBody = buffer.readString(charset);
                    sb.append("\n[" + threadName + "]---> BODY:\n" + requestBody);
                    sb.append("\n[" + threadName + "]---> END " + request.method() + " (" + body.contentLength() + "-byte body)");
                } else {
                    sb.append("\n[" + threadName + "]---> END " + request.method() + " (binary " + body.contentLength() + "-byte body omitted)");
                }
            }
        }
        return requestBody;
    }

    private String getAndAppendResponseBody(String threadName, StringBuffer sb, Response response, long tookMs) throws IOException {
        ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        sb.append("\n[" + threadName + "]<--- " + response.code() + ' ' + response.message() + ' '
                + response.request().url() + " (" + tookMs + "ms" + (", " + bodySize + " body") + ')');
        String str = responseBody.string();
        sb.append("\n[" + threadName + "]<--- RESPONSE BODY:\n" + str);
        sb.append("\n[" + threadName + "]<--- END HTTP\n");
        return str;
    }

    private String getResponseTimeLog(Response response, long tookMs, String requestFinishTime) throws IOException {
        int responseCode = response.code();
        ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        return "Time: " + requestFinishTime + "\n" + "Code: " + responseCode + "\n" + "Url: " + response.request().url() + " (" + tookMs + "ms" + (", " + bodySize + " body") + ')';
    }

    private Response proceedRequest(Chain chain, String threadName, StringBuffer sb, String attachmentName, Request request) throws IOException {
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            sb.append("\n[" + threadName + "]<--- HTTP FAILED: " + e);
            e.printStackTrace();
            writeErrorLog(sb, attachmentName);
            throw e;
        }
        return response;
    }

    private String getAndAppendResponseHeaders(String threadName, StringBuffer sb, Response response) {
        List<String> loggedResponseHeaders = asList("x-request-id");
        String headers = "";
        for (String name : loggedResponseHeaders) {
            try {
                String value = response.header(name);
                sb.append("\n[")
                        .append(threadName)
                        .append("]<--- ")
                        .append(name)
                        .append(": ")
                        .append(value);
                headers += name + ": " + value + "\n";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return headers;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                if (Character.isISOControl(prefix.readUtf8CodePoint())) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false;
        }
    }

    public void writeLog(StringBuffer logs, String requestViewAllure, String responseViewAllure) {
        logger.info(logs.toString());
        logger.info("Allure Test Step " + Allure.getLifecycle().getCurrentTestCaseOrStep().isPresent());
        if (Allure.getLifecycle().getCurrentTestCaseOrStep().isPresent()) {
            String type = "application/json";
            Allure.addAttachment("Request", type, requestViewAllure);
            Allure.addAttachment("Response", type, responseViewAllure);
        }
    }

    public void writeErrorLog(StringBuffer allLogs, String attachmentName) {
        logger.error(allLogs.toString());
        allureTxtAttachment(attachmentName, allLogs.toString());
    }

    @Attachment(value = "{0}", type = "text/plain")
    public static String allureTxtAttachment(String name, String text) {
        return text;
    }
}