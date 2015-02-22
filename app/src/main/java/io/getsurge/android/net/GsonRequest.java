package io.getsurge.android.net;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by danieljulio on 27/10/2014.
 */
public class GsonRequest<T> extends Request<T> {

    private static final String TAG = GsonRequest.class.getSimpleName();

    private final Gson gson = new Gson();

    private final Class<T> clazz;
    private final Map<String, String> headers;
    private Map<String,String> body;
    private final Response.Listener<T> listener;


    public GsonRequest(Builder builder){
        super(builder.method, builder.url, builder.errorListener);
        clazz = builder.clazz;
        headers = builder.headers;
        listener = builder.successListener;
        body = builder.body;
    }


    public static class Builder {

        private int method = Method.GET;
        private String url;
        private Class clazz;
        private Response.Listener successListener;
        private Response.ErrorListener errorListener;
        private Map<String, String> headers;
        private Map<String, String> body;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder clazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder successListener(Response.Listener successListener) {
            this.successListener = successListener;
            return this;
        }

        public Builder errorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public Builder post() {
            this.method = Method.POST;
            return this;
        }

        public Builder method(int method) {
            this.method = method;
            return this;
        }

        public Builder addHeader(String key, String value){
            if(headers == null)
                headers = new HashMap<>();
            headers.put(key, value);
            return this;
        }

        public Builder headers(Map<String, String> headers){
            this.headers = headers;
            return this;
        }

        public Builder body(Map<String, String> body){
            post();
            this.body = body;
            return this;
        }

        public Builder addBody(String key, String value){
            if(body == null) {
                body = new HashMap<>();
                post();
            }
            body.put(key, value);
            return this;
        }

        public GsonRequest build(){
            return new GsonRequest(this);
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Map<String,String> getParams(){
        return body;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);
            if(element.isJsonArray()){
                JsonArray array = (JsonArray) element;
                ArrayList elements = new ArrayList();
                for (int i = 0; i < array.size(); i++) {
                    elements.add(gson.fromJson(array.get(i).toString(), clazz));
                }
                return (Response<T>) Response.success(elements, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.success(
                        gson.fromJson(json, clazz),
                        HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e){
            return Response.error(new ParseError(e));
        }
    }

}
