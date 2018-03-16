package gmocoin.autoFX;

import java.awt.event.*;

import javax.swing.*;

import gmocoin.autoFX.Collabo.*;
import gmocoin.autoFX.Collabo.abs.*;
import gmocoin.autoFX.Collabo.sp.*;
import gmocoin.autoFX.control.*;
import gmocoin.autoFX.windows.*;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        App as = new App();
        as.show();
        // System.out.println( "Hello World!" );
        // ISession session = new SpSession("liangyic@live.cn", "!sonya7m2k!");
        // if(session.getLoginState() == AbsSession.USER_INVALID){
        //
        // }else if (session.isWaitVeriCd()){
        // session.setVeriCd("123456");
        // }
    }

    private JFrame frame = new JFrame("GMO比特币自动交易系统");
    private JFrame veriframe;
    private JFrame statusframe;
    private JButton loginBtn = new JButton("登录");
    private JTextField userNmField;
    private JPasswordField passWordField;
    private Control control;
    ISession session;

    public App() {
        this.control = Control.getInstance();
        frame.setContentPane(new JPanel());
        userNmField = new JTextField(15);
        passWordField = new JPasswordField(15);
        frame.getContentPane().add(userNmField);
        frame.getContentPane().add(passWordField);
        frame.getContentPane().add(loginBtn);

        userNmField.setText(this.control.getProperty("userCd"));
        passWordField.setText(this.control.getProperty("password"));
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                session = new SpSession(userNmField.getText(), passWordField.getText());
                if (session.getLoginState() == AbsSession.USER_INVALID) {
                    JOptionPane.showMessageDialog(null, "用户名或密码错误！");
                } else if (session.isWaitVeriCd()) {
                    veriframe = new inputVerCdWin(session, frame);
                    veriframe.setVisible(true);
                } else {
                    statusframe = new StatusWin(session, frame);
                    frame.setVisible(false);
                }

            }
        });
        frame.setSize(240, 140);
    }

    public void show() {
        frame.setVisible(true);
    }
}
