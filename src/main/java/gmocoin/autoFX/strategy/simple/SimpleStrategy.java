package gmocoin.autoFX.strategy.simple;

import java.util.*;

import gmocoin.autoFX.Collabo.sp.*;
import gmocoin.autoFX.control.*;
import gmocoin.autoFX.strategy.*;
import gmocoin.autoFX.strategy.common.*;

@SuppressWarnings("boxing")
public class SimpleStrategy extends absStrategy {
    private static int BUY_SCORE;
    private static int SETTLE_SCORE;
    private static boolean REFLASH_TEST_PARAM;
    private static int lostCut;
    private static int[] SCORE_PARAM;
    private static Map<String, List<Object>> cache;
    static {
        cache = new HashMap<String, List<Object>>();
        control = Control.getInstance();
        BUY_SCORE = Integer.valueOf(control.getProperty("buyScore"));
        SETTLE_SCORE = Integer.valueOf(control.getProperty("settleScore"));
        SCORE_PARAM = new int[9];
        String[] spStrs = control.getProperty("ScoreParam").split(",");
        for (int i = 0; i < spStrs.length; i++) {
            SCORE_PARAM[i] = Integer.valueOf(spStrs[i]).intValue();
        }
        REFLASH_TEST_PARAM = false;
    }

    private boolean isTest;

    private int backCount = 0;

    private int buyScore;
    private int settleScore;
    private int maxFluct;
    private int maxS;

    private StatisticsData sdata;

    private int[] scoreParam;

    public SimpleStrategy(SpService service, int buyScore, int settleScore, StatisticsData sdata, int[] scoreParam) {
        super(service);
        this.maxTradeSize = 1000;
        this.maxFluct = 1500;
        this.maxS = 3000;

        this.buyScore = buyScore;
        this.settleScore = settleScore;
        this.scoreParam = scoreParam;

        this.sdata = sdata;
        this.isTest = true;
    }

    public SimpleStrategy(SpService service, boolean isTest) {
        super(service);
        this.maxFluct = 1500;
        this.maxS = 3000;
        this.isTest = isTest;
        if (isTest) {
            this.buyScore = BUY_SCORE;
            this.settleScore = SETTLE_SCORE;
            this.scoreParam = SCORE_PARAM;
        }
    }
    public void init(){
    	//
    }
    public void run() {
        if (REFLASH_TEST_PARAM && isTest) {
            this.buyScore = BUY_SCORE;
            this.settleScore = SETTLE_SCORE;
            this.scoreParam = SCORE_PARAM;
            REFLASH_TEST_PARAM = false;
        }
        super.run();
    }

    protected boolean doTread() {
        boolean result = false;
        if (backCount > 0) {
            backCount--;
        }
        if (liveTradeSize < maxTradeSize && backCount == 0) {
            StatisticsData statisticsData = this.sdata;
            int[] param = SCORE_PARAM;

            int sslop75 = (int)(Math.sqrt(Math.abs(statisticsData.getSlop75())));

            int avg25 = statisticsData.getLastAvg25Val();
            int avg75 = statisticsData.getLastAvg75Val();
            int price = 0;
            int score = 0;
            int avg5 = statisticsData.getLastAvg5Val();
            int prevAvg5 = statisticsData.getbAvg5Val(10);
            int prevAvg25 = statisticsData.getbAvg25Val(10);
            int prevAvg75 = statisticsData.getbAvg75Val(10);

            score = isBuy(statisticsData);

            score += (statisticsData.getSlop75() > 0.0 ? sslop75 : -sslop75);
            if ((avg25 - avg75) > 3000) {
                score += param[7];
            }
            if ((prevAvg5 < prevAvg75) && (avg5 > prevAvg75)) {
                score += 5;
            }
            if ((prevAvg25 < prevAvg75) && (avg25 > prevAvg75)) {
                score += 15;
            }
            price = (int) (statisticsData.getCurrent() + DIFFERENCE / 2);
            // System.out.println("doTread score:"+score + " " +"多");
            newTrade(score, true, statisticsData, isTest ? buyScore : BUY_SCORE);

            score = isShort(statisticsData);
            score += (statisticsData.getSlop75() > 0.0 ? -sslop75 : sslop75);
            if ((avg75 - avg25) > 3000) {
                score += param[7];
            }
            if ((prevAvg5 > prevAvg75) && (avg5 < prevAvg75)) {
                score += 5;
            }
            if ((prevAvg25 > prevAvg75) && (avg25 < prevAvg75)) {
                score += 15;
            }
            price = (int) (statisticsData.getCurrent() - DIFFERENCE / 2);
            // System.out.println("doTread score:"+score + " " +("空"));
            newTrade(score - 5, false, statisticsData, isTest ? buyScore : BUY_SCORE);

        }
        return result;
    }

    private void newTrade(int score, boolean isbuy, StatisticsData statisticsData, int passedScore) {
        if (score > passedScore) {
            int price = 0;
            if (isbuy) {
                price = (int) (statisticsData.getCurrent() + DIFFERENCE / 2);
            } else {
                price = (int) (statisticsData.getCurrent() - DIFFERENCE / 2);
            }

            if (!this.isTest) {
                System.out.println("新建交易:" + (isbuy ? "多" : "空") + " score:" + score);
                System.out.println("Trend5:" + statisticsData.getTrend5());
                System.out.println("Trend25:" + statisticsData.getTrend25());
                System.out.println("Trend75:" + statisticsData.getTrend75());
                System.out.println("Trend5:" + statisticsData.getSlop5());
                System.out.println("Trend25:" + statisticsData.getSlop25());
                System.out.println("Trend75:" + statisticsData.getSlop75());
            }
            Trade trade;
            if (isTest) {
                trade = new Trade(isbuy, statisticsData.getLastVal(), price, this.singleQuantity);
            } else {
                trade = service.newTread(isbuy, this.singleQuantity);
            }
            if (trade != null) {
                tradeList.add(trade);
                liveTradeSize++;
                if (isTest) {
                    backCount = 2;
                } else {
                    backCount = 3000;
                }
            }
        }
    }

    protected boolean doSettlement() {
        int[] param = SCORE_PARAM;
        if (isTest) {
            param = this.scoreParam;
        }
        boolean result = false;
        StatisticsData statisticsData = this.sdata;
        int currentPrice = (int) statisticsData.getCurrent();
        int price = 0;
        int prof = (int)(30f * 100.0 * this.singleQuantity);
        int avg5 = statisticsData.getLastAvg5Val();
        int avg25 = statisticsData.getLastAvg25Val();
        int avg75 = statisticsData.getLastAvg75Val();
        int prevAvg5 = statisticsData.getbAvg5Val(10);
        int prevAvg25 = statisticsData.getbAvg25Val(10);
        int prevAvg75 = statisticsData.getbAvg75Val(10);

        int buyScore = 0;
        int sellScore = 0;
        if (tradeList.size() > 0) {
            buyScore = isShort(statisticsData);
            if ((avg25 - avg75) > 2500) {
                buyScore += param[7];
            }
            if ((prevAvg5 < prevAvg75) && (avg5 > prevAvg75)) {
                buyScore += 5;
            }
            if ((prevAvg25 < prevAvg75) && (avg25 > prevAvg75)) {
                buyScore += 15;
            }
            sellScore = isBuy(statisticsData);
            if ((avg75 - avg25) > 2500) {
                sellScore += param[7];
            }
            if ((prevAvg5 > prevAvg75) && (avg5 < prevAvg75)) {
                sellScore += 5;
            }
            if ((prevAvg25 > prevAvg75) && (avg25 < prevAvg75)) {
                sellScore += 15;
            }
        }
        for (Trade trade : tradeList) {
            if (!trade.isSettlement()) {
                boolean doSettle = false;
                int score = 0;
                int cut = 0;
                if (trade.isBuy()) {
                    price = currentPrice - DIFFERENCE / 2;
                    score = buyScore;
                    cut = 100 - currentPrice * 100 / trade.getPrice();
                } else {
                    price = currentPrice + DIFFERENCE / 2;
                    score = sellScore;
                    cut = currentPrice * 100 / trade.getPrice() - 100;
                }

                if (score > (this.isTest ? settleScore : SETTLE_SCORE)) {
                    if (trade.getProfitLoss(price) > prof) {
                        doSettle = true;
                    }
                } else if (cut >= lostCut) {
                    doSettle = true;
                }

                // System.out.println("Settle score:"+score + " " + (trade.isBuy()?"多":"空"));

                if (doSettle) {
                    if (!this.isTest) {
                        System.out.println("新建決済" + " score:" + score);
                        System.out.println("Trend5:" + statisticsData.getTrend5());
                        System.out.println("Trend25:" + statisticsData.getTrend25());
                        System.out.println("Trend75:" + statisticsData.getTrend75());
                        System.out.println("Trend5:" + statisticsData.getSlop5());
                        System.out.println("Trend25:" + statisticsData.getSlop25());
                        System.out.println("Trend75:" + statisticsData.getSlop75());
                    }
                    if (isTest) {
                        trade.doSettlement(price);
                        liveTradeSize--;
                    } else {
                        if (service.settleTread(trade)) {
                            liveTradeSize--;
                        }
                    }
                }
            }
        }
        return result;
    }

    private int isBuy(StatisticsData statisticsData) {
        int flg = 0;
        int[] param = SCORE_PARAM;
        if (isTest) {
            param = this.scoreParam;
        }

        PriceData currentDate = statisticsData.getLastVal();
        int currentPrice = (int) statisticsData.getCurrent();
        int avg5 = statisticsData.getLastAvg5Val();
        int avg25 = statisticsData.getLastAvg25Val();
        int avg75 = statisticsData.getLastAvg75Val();
        int s25 = statisticsData.getS25();
        int s5 = statisticsData.getS5();
        int s525 = ((s25 + s5 * 3) >> 2);
        int hosei = 0;
        int sslop5 = (int)Math.pow(Math.abs(statisticsData.getSlop5()), 0.333);
        int sslop25 = (int)Math.pow(Math.abs(statisticsData.getSlop25()), 0.333);
        if (statisticsData.getTrend75() == StatisticsData.RISE) {
            hosei = s5 / 3;
        }
        if (statisticsData.getTrend75() == StatisticsData.DECLINE) {
            hosei = -s5 / 3;
        }
        if (s25 < this.maxS) {
            flg += param[0];
        }
        if ((avg25 - currentPrice + hosei) > s525) {
            flg += param[1];
        }
        if (statisticsData.getTrend25() == StatisticsData.CONCAVITY
                || statisticsData.getTrend25() == StatisticsData.RISE) {
            flg += param[2];
        }
        if (statisticsData.getTrend5() == StatisticsData.RISE) {
            flg += param[3];
        }

        if (statisticsData.getTrend25() == StatisticsData.DECLINE) {
            flg -= param[4];
        }
        if (statisticsData.getTrend5() == StatisticsData.DECLINE) {
            flg -= param[5];
        }

        if (statisticsData.getSlop5() > 0.0 && statisticsData.getSlop5() * statisticsData.getPrevSlop5() < 0.0) {
            flg += param[6];
        }

        flg += (statisticsData.getSlop5() > 0.0 ? sslop5 : -sslop5);
        flg += (statisticsData.getSlop25() > 0.0 ? sslop25 : -sslop25);

        return flg;
    }

    private int isShort(StatisticsData statisticsData) {
        int flg = 0;
        int[] param = SCORE_PARAM;
        if (isTest) {
            param = this.scoreParam;
        }
        PriceData currentDate = statisticsData.getLastVal();
        int currentPrice = (int) statisticsData.getCurrent();
        int avg5 = statisticsData.getLastAvg5Val();
        int avg25 = statisticsData.getLastAvg25Val();
        int avg75 = statisticsData.getLastAvg75Val();
        int s25 = statisticsData.getS25();
        int s5 = statisticsData.getS5();
        int s525 = ((s25 + s5 * 3) >> 2);
        int hosei = 0;
        int sslop5 = (int)Math.pow(Math.abs(statisticsData.getSlop5()), 0.333);
        int sslop25 = (int)Math.pow(Math.abs(statisticsData.getSlop25()), 0.333);
        if (statisticsData.getTrend75() == StatisticsData.RISE) {
            hosei = s5 / 2;
        }
        if (statisticsData.getTrend75() == StatisticsData.DECLINE) {
            hosei = -s5 / 2;
        }
        if (s25 < this.maxS) {
            flg += param[0];
        }
        if ((currentPrice - avg25 - hosei) > s525) {
            flg += param[1];
        }
        if (statisticsData.getTrend25() == StatisticsData.CONVEX
                || statisticsData.getTrend25() == StatisticsData.DECLINE) {
            flg += param[2];
        }
        if (statisticsData.getTrend5() == StatisticsData.DECLINE) {
            flg += param[3];
        }

        if (statisticsData.getTrend25() == StatisticsData.RISE) {
            flg -= param[4];
        }
        if (statisticsData.getTrend5() == StatisticsData.RISE) {
            flg -= param[5];
        }

        if (statisticsData.getSlop5() < 0.0 && statisticsData.getSlop5() * statisticsData.getPrevSlop5() < 0.0) {
            flg += param[6];
        }

        flg += (statisticsData.getSlop5() < 0.0 ? sslop5 : -sslop5);
        flg += (statisticsData.getSlop25() < 0.0 ? sslop25 : -sslop25);

        return flg;
    }

    public static void setBuyScore(int buyScore) {
        BUY_SCORE = buyScore;
    }

    public static void setSettleScore(int maxSettle) {
        SETTLE_SCORE = maxSettle;
    }

    public static void setScoreParam(int[] scoreParam) {
        SCORE_PARAM = scoreParam;
    }

    public static void refushTestParam() {
        REFLASH_TEST_PARAM = true;
    }

    /******************************************************
     * 训练参数
     ******************************************************/
    private int maxScore;
    private int maxProfit;
    private int maxBuyScore;
    private int maxSettleScore;
    private int maxLostPersent;
    private int maxSettledTradeCount;
    private int[] maxScoreParam;
    private static final int SPAN = 3;

    @Override

    public void traing() {
        this.maxScore = 0;
        this.maxProfit = 0;
        this.maxBuyScore = 60;
        this.maxSettleScore = 40;
        this.maxLostPersent = 0;
        this.maxSettledTradeCount = 0;
        this.maxScoreParam = new int[9];
        for (int i = 40; i < 61; i += (SPAN << 1)) {
            final int s = i;
            final int e = i + (SPAN << 1);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    optimisation(s, e);
                }
            });
            t.start();
        }
    }

    private void optimisation(int sSettleScore, int eSettleScore) {
        List<PriceData> dataList = this.sdata.getDataList();

        int profit = 0;
        int SettleScore = sSettleScore;
        int lostPersent = 0;
        int[] ScoreParam = new int[9];
        int FO = 0;
        for (; SettleScore < eSettleScore; SettleScore += (SPAN << 1)) {
            for (ScoreParam[0] = 0; ScoreParam[0] <= 30; ScoreParam[0] += SPAN) {
                for (ScoreParam[1] = 0; ScoreParam[1] <= 30; ScoreParam[1] += SPAN) {
                    for (ScoreParam[2] = 0; ScoreParam[2] <= 30; ScoreParam[2] += SPAN) {
                        for (ScoreParam[3] = 0; ScoreParam[3] <= 30; ScoreParam[3] += SPAN) {
                            for (ScoreParam[4] = 0; ScoreParam[4] <= 30; ScoreParam[4] += SPAN) {
                                for (ScoreParam[5] = 0; ScoreParam[5] <= 30; ScoreParam[5] += SPAN) {
                                    for (ScoreParam[6] = 0; ScoreParam[6] <= 30; ScoreParam[6] += SPAN) {
                                        for (ScoreParam[7] = 0; ScoreParam[7] <= 20; ScoreParam[7] += SPAN) {
                                            int mScore = ScoreParam[0]
                                                    + ScoreParam[1]
                                                    + ScoreParam[2]
                                                    + ScoreParam[3]
                                                    - ScoreParam[4]
                                                    - ScoreParam[5]
                                                    + ScoreParam[6]
                                                    + ScoreParam[7];
                                            if (mScore <= (this.maxBuyScore + 10)) {
                                                continue;
                                            } else if (mScore >= 90) {
                                                break;
                                            }
                                            FO++;
                                            StatisticsData testData = StatisticsData.getTestInstance();
                                            IStrategy strategy = new SimpleStrategy(service, this.maxBuyScore,
                                                    SettleScore, testData, ScoreParam);

                                            for (int i = 0; i < dataList.size() - 1; i++) {
                                                PriceData data = dataList.get(i);
                                                testData.add(data);
                                                if (i > 150) {
                                                    strategy.run();
                                                }
                                            }
                                            int settledTcount = strategy.getSettledTradeCount();
                                            int livedTcount = strategy.getLivededTradeCount();
                                            if (settledTcount > 0) {
                                                lostPersent = strategy.getLostCutTradeCount() * 100 / settledTcount;
                                            } else {
                                                lostPersent = 100;
                                            }

                                            profit = strategy.getProfitLoss();

                                            int score = profit
                                                    - ((lostPersent * lostPersent) << 4)
                                                    + (settledTcount << 3);

                                            if ((FO % 1000) == 0) {
                                                StringBuffer sb = new StringBuffer();
                                                for (int i = 0; i < ScoreParam.length; i++) {
                                                    sb.append(ScoreParam[i]).append(",");
                                                }
                                                System.out.println("FO:" + FO);
                                                System.out.println("ScoreParam:" + sb.toString());
                                                System.out.println("---------------------------------------------");
                                            }
                                            if (score > this.maxScore) {
                                                this.maxScore = score;
                                                this.maxProfit = profit;
                                                this.maxLostPersent = lostPersent;
                                                this.maxSettleScore = SettleScore;
                                                this.maxSettledTradeCount = settledTcount;
                                                StringBuffer sb = new StringBuffer();
                                                for (int i = 0; i < ScoreParam.length; i++) {
                                                    this.maxScoreParam[i] = ScoreParam[i];
                                                    sb.append(ScoreParam[i]).append(",");
                                                }
                                                System.out.println("---------------------------------------------");
                                                System.out.println("maxProfit:" + this.maxProfit);
                                                System.out.println("maxBuyScore:" + this.maxBuyScore);
                                                System.out.println("maxSettleScore:" + this.maxSettleScore);
                                                System.out.println("maxLostPersent:" + this.maxLostPersent);
                                                System.out.println("maxScoreParam:" + sb.toString());
                                                System.out.println("maxSettledTradeCount:" + this.maxSettledTradeCount);
                                                System.out.println("livedTcount:" + livedTcount);
                                                System.out.println("FO:" + FO);
                                                System.out.println("---------------------------------------------");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.maxScoreParam.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(this.maxScoreParam[i]);
        }
        System.out.println("-----------------最適化-----------------------");
        System.out.println("maxScore:" + this.maxScore);
        System.out.println("maxProfit:" + this.maxProfit);
        System.out.println("maxBuyScore:" + this.maxBuyScore);
        System.out.println("maxSettleScore:" + this.maxSettleScore);
        System.out.println("maxLostPersent:" + this.maxLostPersent);
        System.out.println("maxScoreParam:" + sb.toString());
        System.out.println("maxSettledTradeCount:" + this.maxSettledTradeCount);
        System.out.println("计算复杂度:" + FO);
        System.out.println("------------------END------------------------");
        if (this.maxScore > 1000) {
            setBuyScore(this.maxBuyScore);
            setSettleScore(this.maxSettleScore);
            setScoreParam(this.maxScoreParam);
            refushTestParam();

            control.setProperty("buyScore", this.maxBuyScore + "");
            control.setProperty("settleScore", this.maxSettleScore + "");
            control.setProperty("ScoreParam", sb.toString());
        } else {
            // 没找到最佳参数，不新建交易
            setBuyScore(1000);
        }
    }
}
