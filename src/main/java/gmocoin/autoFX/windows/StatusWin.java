package gmocoin.autoFX.windows;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import gmocoin.autoFX.Collabo.*;
import gmocoin.autoFX.Collabo.sp.*;
import gmocoin.autoFX.strategy.common.*;

public class StatusWin extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    ISession session;
    private JFrame loginFrame;
    private JButton logoutBtn = new JButton("登出");
    private Thread serviceThread;
    private JLabel namelabel = new JLabel("");
    private JLabel currentPLabel = new JLabel("");
    private JLabel profitLossPLabel = new JLabel("");
    private JLabel paramLabel = new JLabel("");
    private JList<String> tradeJList = new JList<String>();

    public StatusWin(final ISession session, final JFrame loginFrame) {
        super.setTitle("GMO比特币自动交易系统");
        this.loginFrame = loginFrame;
        this.session = session;
        UserInfo userInfo = (UserInfo)session.getAttribute("userInfo");
        final SpService service = (SpService)session.getService();
        serviceThread = new Thread(service);
        this.setContentPane(new JPanel());
        this.getContentPane().add(namelabel);
        if (userInfo != null) {
            namelabel.setText(userInfo.getLastName() + " " + userInfo.getFirstName());
        } else {
            namelabel.setText("系统维护，请稍后再试。");
        }
        this.getContentPane().add(currentPLabel);
        this.getContentPane().add(profitLossPLabel);
        this.getContentPane().add(paramLabel);
        this.getContentPane().add(tradeJList);
        this.getContentPane().add(logoutBtn);
        final JFrame self = this;
        logoutBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                serviceThread.stop();
                session.invalidate();
                self.setVisible(false);
                loginFrame.setVisible(true);
            }
        });
        if (userInfo != null) {
            try {
                serviceThread.start();
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            new Thread(new Runnable() {

                public void run() {
                    while (true) {
                        try {
                            int curVal = (int) service.getData().getCurrent();
                            currentPLabel.setText("现价：" + curVal);
                            profitLossPLabel.setText("总损益："
                                    + service.getProfitLoss()
                                    + " 未決済损益："
                                    + service.getVaildProfitLoss());
                            paramLabel.setText("有效单数："
                                    + service.getValidTradeCount()
                                    + " 成交单数："
                                    + service.getSettledTradeCount());
                            List<Trade> tradeList = service.getValidTradeList();
                            String[] strList = new String[tradeList.size()];
                            int i = 0;
                            for (Trade trade : tradeList) {
                                int p = trade.isBuy() ? (curVal - 750) : (curVal + 750);
                                String str = "买价："
                                        + trade.getPrice()
                                        + " 收益："
                                        + trade.getProfitLoss(p)
                                        + (trade.isBuy() ? " 多" : " 空");
                                strList[i] = str;
                                i++;
                            }
                            tradeJList.setListData(strList);
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
        }
        this.setVisible(true);
        this.setSize(240, 240);
    }
}
