package gmocoin.autoFX.Collabo.common;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;


import gmocoin.autoFX.Collabo.IHeadParams;
import gmocoin.autoFX.Collabo.ISession;

public class ReadHtml {
    static final int BUFFER_SIZE = 1024;

    private IHeadParams heads;

    public ReadHtml(IHeadParams heads) {
        this.heads = heads;
    }

    public Map<String, List<String>> sendGet(String strUrl, Map<String, String> attr, String cookie,
            StringBuffer html) {

        URL url = null;
        final char[] buffer = new char[BUFFER_SIZE];
        InputStream is = null;
        StringBuffer params = new StringBuffer();
        Map<String, List<String>> header = null;
        int index = 0;
        for (String key : attr.keySet()) {
            if (index > 0) {
                params.append("&");
            }
            params.append(key);
            params.append("=");
            params.append(attr.get(key));
            index++;
        }
        try {
            if (index > 0) {
                strUrl = strUrl + "?" + params.toString();
            }
            url = new URL(strUrl);
            HttpURLConnection urlcon = (HttpURLConnection)url.openConnection();

            urlcon.setRequestProperty("accept", "*/*");
            urlcon.setRequestProperty("connection", "Keep-Alive");
            urlcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlcon.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            urlcon.setRequestProperty("Origin", heads.getOrigin());
            urlcon.setRequestProperty("Referer", heads.getReferer());
            if (cookie != null && !"".equals(cookie)) {
                urlcon.setRequestProperty("Cookie", cookie);
            }
            urlcon.connect();
            // 获取所有响应头字段
            header = urlcon.getHeaderFields();
            is = urlcon.getInputStream();
            final Reader in = new InputStreamReader(is, "UTF-8");
            for (;;) {
                int rsz = in.read(buffer, 0, BUFFER_SIZE);
                if (rsz < 0) {
                    break;
                }
                html.append(buffer, 0, rsz);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                //
            }
        }
        return header;
    }

    public Map<String, List<String>> sendPost(String strUrl, Map<String, String> attr, String cookie,
            StringBuffer html) {

        URL url = null;
        final char[] buffer = new char[BUFFER_SIZE];
        InputStream is = null;
        PrintWriter out = null;
        StringBuffer params = new StringBuffer();
        int index = 0;
        Map<String, List<String>> header = null;
        for (String key : attr.keySet()) {
            if (index > 0) {
                params.append("&");
            }
            params.append(key);
            params.append("=");
            params.append(attr.get(key));
            index++;
        }
        try {
            url = new URL(strUrl);
            HttpURLConnection urlcon = (HttpURLConnection)url.openConnection();

            urlcon.setRequestProperty("accept", "*/*");
            urlcon.setRequestProperty("connection", "Keep-Alive");
            urlcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlcon.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            urlcon.setRequestProperty("Origin", heads.getOrigin());
            urlcon.setRequestProperty("Referer", heads.getReferer());
            if (cookie != null && !"".equals(cookie)) {
                urlcon.setRequestProperty("Cookie", cookie);
            } else {
                urlcon.setInstanceFollowRedirects(false);
            }
            urlcon.setDoOutput(true);
            urlcon.setDoInput(true);
            out = new PrintWriter(urlcon.getOutputStream());
            out.print(params.toString());
            out.flush();
            // 获取所有响应头字段
            header = urlcon.getHeaderFields();

            is = urlcon.getInputStream();
            final Reader in = new InputStreamReader(is, "UTF-8");
            for (;;) {
                int rsz = in.read(buffer, 0, BUFFER_SIZE);
                if (rsz < 0) {
                    break;
                }
                html.append(buffer, 0, rsz);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                //
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
            }
        }
        return header;
    }
    
    public Map<String, List<String>> sendPost(String strUrl, JSONObject attrJson, String cookie,
            StringBuffer html) {

        URL url = null;
        final char[] buffer = new char[BUFFER_SIZE];
        InputStream is = null;
        PrintWriter out = null;
        StringBuffer params = new StringBuffer(attrJson.toString());
        Map<String, List<String>> header = null;
        try {
            url = new URL(strUrl);
            HttpURLConnection urlcon = (HttpURLConnection)url.openConnection();

            urlcon.setRequestProperty("accept", "*/*");
            urlcon.setRequestProperty("connection", "Keep-Alive");
            urlcon.setRequestProperty("Content-Type", "application/json");
            urlcon.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            urlcon.setRequestProperty("Origin", heads.getOrigin());
            urlcon.setRequestProperty("Referer", heads.getReferer());
            if (cookie != null && !"".equals(cookie)) {
                urlcon.setRequestProperty("Cookie", cookie);
            } else {
                urlcon.setInstanceFollowRedirects(false);
            }
            urlcon.setDoOutput(true);
            urlcon.setDoInput(true);
            out = new PrintWriter(urlcon.getOutputStream());
            out.print(params.toString());
            out.flush();
            // 获取所有响应头字段
            header = urlcon.getHeaderFields();

            is = urlcon.getInputStream();
            final Reader in = new InputStreamReader(is, "UTF-8");
            for (;;) {
                int rsz = in.read(buffer, 0, BUFFER_SIZE);
                if (rsz < 0) {
                    break;
                }
                html.append(buffer, 0, rsz);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                //
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
            }
        }
        return header;
    }

    public boolean testSession(String url, ISession session) {
        boolean result = true;
        StringBuffer html = new StringBuffer();
        Map<String, List<String>> header = sendGet(url, new HashMap<String, String>(), session.getSessionId(), html);
        if (header == null ){
			try {
				JSONObject json = new JSONObject(html);
				if (json.getInt("status") != 0) {
					result = false;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return result;
    }

    public String getNewSession(String url) {
        Map<String, List<String>> header = sendGet(url, new HashMap<String, String>(), "", new StringBuffer());
        List<String> cookie = header.get("Set-Cookie");
        String sessionId = "";
        if (cookie != null) {
            sessionId = cookie.get(0).split(";")[0].trim();
        }
        return sessionId;
    }

}
