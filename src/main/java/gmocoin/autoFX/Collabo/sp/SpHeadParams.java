package gmocoin.autoFX.Collabo.sp;

import gmocoin.autoFX.Collabo.IHeadParams;

public class SpHeadParams implements IHeadParams {
    private final String Origin = SpComConstants.SITE_URL;
    private final String Referer = "https://coin.z.com/jp/member/login";

    public String getOrigin() {
        return Origin;
    }

    public String getReferer() {
        return Referer;
    }
}
