package gmocoin.autoFX.Collabo.sp;

import java.util.*;

import org.json.*;

import gmocoin.autoFX.Collabo.abs.*;
import gmocoin.autoFX.control.*;

public class SpSession extends AbsSession {

    public static final String KEY = "SpSession";
    private String authToken;
    private String veriCd;
    private Control control;

    public SpSession(String usercd, String pass) {
        super(usercd, pass, new SpHeadParams());
        this.control = Control.getInstance();
        setSessionId(control.getProperty("SessionId"));
        if (isLoginSuccess()) {
            this.service = new SpService(this);
        }
    }

    public void setVeriCd(String veriCd) {
        this.veriCd = veriCd;
        reLogin();
        if (isLoginSuccess()) {
            this.service = new SpService(this);
        }
    }

    public int doLogin() {
        JSONObject ParamJson = new JSONObject();
        StringBuffer html = new StringBuffer();
        try {
            if (this.authToken != null && !"".equals(this.authToken) && this.veriCd != null) {
                ParamJson.put("authToken", this.authToken);
                ParamJson.put("isTrust", 1);
                ParamJson.put("pinCode", this.veriCd);
                String newSessionId = "";
                Map<String, List<String>> header = readHtml.sendPost(SpComConstants.VERI_URL, ParamJson, getSessionId(),
                        html);
                JSONObject resJson = new JSONObject(html.toString());
                if (header == null) {
                    // @spアクセス失敗
                    return REQ_ERROR;
                }
                JSONObject resJson1 = new JSONObject(html.toString());
                if (resJson1.getInt("status") == 1) {
                    // ログイン失敗、アカウント不正
                    System.out.println("验证码无效");
                    return USER_INVALID;
                } else if (resJson1.getInt("status") == 0) {
                    // ログイン成功
                    List<String> cookie = header.get("Set-Cookie");
                    Optional<String> oSession = cookie.stream().filter(str -> str.contains("GSESSION=")).findFirst();
                    Optional<String> oAccount = cookie.stream().filter(str -> str.contains("ACCOUNT=")).findFirst();
                    Optional<String> oAppli = cookie.stream().filter(str -> str.contains("APPLICATION-")).findFirst();
                    if (oSession.isPresent()) {
                        newSessionId = oSession.get().split(";")[0];
                        if (oAccount.isPresent()) {
                            newSessionId += ";";
                            newSessionId += oAccount.get().split(";")[0];
                        }
                        if (oAppli.isPresent()) {
                            newSessionId += ";";
                            newSessionId += oAppli.get().split(";")[0];
                        }
                        setSessionId(newSessionId);
                        if (readHtml.testSession(SpComConstants.HOME_URL, this)) {
                            JSONObject userJson = new JSONObject(
                                    this.sendGet(SpComConstants.HOME_URL, new HashMap<>()).toString());
                            UserInfo userInfo = new UserInfo(userJson.getJSONObject("data"));
                            this.setAttribute("userInfo", userInfo);
                            control.setProperty("SessionId", getSessionId());
                            return LOGIN_SUCCESS;
                        }
                    }
                }
            } else {
                ParamJson.put("loginId", this.usercd);
                ParamJson.put("password", this.pass);

                String newSessionId = "";
                Map<String, List<String>> header = readHtml.sendPost(SpComConstants.LOGIN_URL, ParamJson,
                        getSessionId(), html);
                JSONObject resJson = new JSONObject(html.toString());
                if (header == null) {
                    // @spアクセス失敗
                    return REQ_ERROR;
                }
                if (resJson.getInt("status") == 1) {
                    // ログイン失敗、アカウント不正
                    System.out.println("ログイン失敗");
                    return USER_INVALID;
                } else if (resJson.getInt("status") == 0) {
                    // ログイン成功
                    this.authToken = resJson.getJSONObject("data").getString("authToken");
                    if (this.authToken != null && !"".equals(this.authToken)) {
                        return WAIT_VERI_CD;
                    } else {
                        // ログイン成功
                        List<String> cookie = header.get("Set-Cookie");
                        Optional<String> oSession = cookie.stream().filter(str -> str.contains("GSESSION="))
                                .findFirst();
                        Optional<String> oAccount = cookie.stream().filter(str -> str.contains("ACCOUNT=")).findFirst();
                        Optional<String> oAppli = cookie.stream().filter(str -> str.contains("APPLICATION-"))
                                .findFirst();
                        if (oSession.isPresent()) {
                            newSessionId = oSession.get().split(";")[0];
                            if (oAccount.isPresent()) {
                                newSessionId += ";";
                                newSessionId += oAccount.get().split(";")[0];
                            }
                            if (oAppli.isPresent()) {
                                newSessionId += ";";
                                newSessionId += oAppli.get().split(";")[0];
                            }
                            setSessionId(newSessionId);
                            if (readHtml.testSession(SpComConstants.HOME_URL, this)) {
                                JSONObject userJson = new JSONObject(
                                        this.sendGet(SpComConstants.HOME_URL, new HashMap<>()));
                                UserInfo userInfo = new UserInfo(userJson.getJSONObject("data"));
                                this.setAttribute("userInfo", userInfo);
                                control.setProperty("SessionId", getSessionId());
                                return LOGIN_SUCCESS;
                            }
                        }
                    }
                }
                return REQ_ERROR;
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return REQ_ERROR;
        }
        return REQ_ERROR;
    }

    public boolean isTimeOut() {
        return !readHtml.testSession(SpComConstants.HOME_URL, this);
    }

    public void invalidate() {
        readHtml.testSession(SpComConstants.LOGIN_OUT_URL, this);
    }

}
