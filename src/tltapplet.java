import gimbalcom.rpc.RemoteFactory;
import gimbalcom.rpc.RpcRemoteIntegerFactory;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.mbed.RPC.HTTPRPC;

public class tltapplet extends Applet {
    
    /**
     * Small class to draw rounded rectangles as an 'LED'
     * Note use of LedColor (as opposed to BackgroundColor) as I haven't been smart enough to work out how to stop Panel
     * painting (hence leave BackgroundColor alone).
     * Also use of ForegroundColor as border of the LED.
     * 
     * Being a UI component, changing LedColor repaints if necessary.
     */
    public class LedPanel extends Panel {
        private static final long serialVersionUID = 1L;
        private int radius;
        private Color ledColor = getBackground();
        private Color borderColor = getBackground();

        @Override
        public void paint( Graphics g) {
            Dimension sz = getSize();
            g.setColor(ledColor);
            g.fillRoundRect(1, 1, sz.width-2, sz.height-2, radius, radius);
            g.setColor(borderColor);
            g.drawRoundRect(0, 0, sz.width-1, sz.height-1, radius, radius);
        }
        
        public void setRadius(int value) {
            radius = value;
        }
        
        public void setLedColor(Color value) {
            boolean changed = ledColor != value;
            ledColor = value;
            if (changed)
                repaint();
        }
        
        public void setBorderColor(Color value) {
            boolean changed = borderColor != value;
            borderColor = value;
            if (changed)
                repaint();
        }
    }

    HTTPRPC mbed;
    boolean threadSuspended;
    Timer refresh_timer;
    private static final long serialVersionUID = 1L;

    // setup local and rpc variables
    RemoteFactory.Integer CtrlAction;
    RemoteFactory.Integer LEDStatus;
    RemoteFactory.Integer SynthFrequencyActual;
    RemoteFactory.Integer SynthFrequencyUpdate;
    RemoteFactory.Integer AttenuatorActual;
    RemoteFactory.Integer AttenuatorUpdate;

    RemoteFactory.Integer minFrequencyMHz;
    RemoteFactory.Integer maxFrequencyMHz;

    RemoteFactory.Integer ipAddrRpc;
    RemoteFactory.Integer ipMaskRpc;

    // screen position coordinates for drawing LEDs, Btns and text
    private static final int LED1_y = 21;
    private static final int LED2_y = 321;
    private static final int LED3_y = 371;
    private static final int LED_x = 20;
    private static final int LED_dx = 40;
    private static final int LED_dy = 28;
    private static final int LED_r = 6;

    boolean frontPanelControlled = false;
    boolean isPLO = false;
    int SynthLockLED_i = 0;

    int PSU1Alarm_i = 0;
    int SynthType_i = 0;
    int AttType_i = 0;
    int SerialAlarm_i = 0;
    private boolean ploOscAlarm;

    int SynthFrequency;
    int FreqUpdateIcon = 0; // displayed if the frequency has been changed
    int F_INC = 25; // increment value in MHz
    int SYNTH_FREQ_MIN;
    int SYNTH_FREQ_MAX;

    int Attenuation;
    int AttUpdateIcon = 0; // displayed if the attenuator has been changed
    int A_INC = 1; // increment value in bits
    int A_INCx = 4; // increment value in bits
    int A_INCxx = 40; // increment value in bits
    int ATT_MIN = 0;
    int attenuationMax;

    int CtrlStatusData = 0;
    int CommsOpenFlag = 0;
    int comms_active = 0;
    int connection_ctr = 0;
    int update_ctr = 0;

    int ipAddr;
    int ipMask;

    Font smallFont = new Font("Arial", Font.PLAIN, 13);
    Font bigFont = new Font("Arial", Font.PLAIN, 32);

    Button Enter_ALBtn;
    Button Increase_ALBtn;
    Button Decrease_ALBtn;
    
    LedPanel localRemoteLed;
    LedPanel synthLockLed;
    LedPanel psuAlarmLed;
    
    Label synthValueLabel;
    Label attnValueLabel;
    Label connectionCounterLabel;
    
    TextField ipAddrField = new TextField(20);
    TextField ipMaskField = new TextField(20);
    Label ipErrorLabel;
    Button ipSet;

    // **************************************************************************
    // * function to initialise
    // *
    @Override
    public void init() {

        setLayout(null);
        String url = getParameter("url");

        if (url == null) {
            System.out.println("Applet starting on this");
            mbed = new HTTPRPC(this);
        } else {
            System.out.println("Applet starting on " + url);
            mbed = new HTTPRPC(url);
        }

        RemoteFactory factory = new RpcRemoteIntegerFactory(mbed);
                // Use this for off line experiments: new DummyRemoteIntegerFactory(); 

        LEDStatus = factory.create("RemoteLEDStatus");
        int ledStatusI = LEDStatus.read_int();
        System.out.format("LEDStatus %04X\n", ledStatusI);
        CommsOpenFlag = (ledStatusI >> 12) & 0x01;
        
        LedPanel container = new LedPanel();
        container.setLayout(null);
        container.setRadius(20);
        container.setBounds(1, 1, 260, 590);
        container.setBorderColor(Color.BLUE);
        container.setLedColor(Color.WHITE);
        add(container);

        if (CommsOpenFlag == 0) {
            CtrlAction = factory.create("RemoteCtrlAction");
            CtrlAction.write(0x01); // 01=Set Remote Comms Open/Active
            comms_active = 1;

            SynthFrequencyActual = factory.create("RemoteSynthFrequencyActual");
            SynthFrequencyUpdate = factory.create("RemoteSynthFrequencyUpdate");
            AttenuatorActual = factory.create("RemoteAttenuatorActual");
            AttenuatorUpdate = factory.create("RemoteAttenuatorUpdate");

            minFrequencyMHz = factory.create("RemoteMinFreqMHz");
            maxFrequencyMHz = factory.create("RemoteMaxFreqMHz");

            ipAddrRpc = factory.create("IPAddr");
            ipMaskRpc = factory.create("IPMask");

            int rate;
            try {
                rate = Integer.parseInt(getParameter("rate"));
            } catch (Exception e) {
                System.err.println("No parameter found");
                rate = 1000;
            }
            refresh_timer = new Timer(rate, timerListener);
            refresh_timer.start();

            // ======== Create & initialise UI components - roughly in y, x order ========
            // Top row - local/remote LED & button
            localRemoteLed = initLed(LED1_y, Color.ORANGE, container);
            initButton(80, 20, 160, 30, "Local / Remote", container, event -> {
                CtrlAction.write(0x03); // LR on
                CtrlAction.write(0x04); // LR off
                get_data();
            });

            // 2nd row - synth frequency
            synthValueLabel = initLabel(35, 60, 200, 40, bigFont, "unknown", container);
            
            // 3rd row - enter, inc & dec buttons for frequency
            Enter_ALBtn = initButton(20, 120, 60, 30, "Enter", container, event -> {
                enterAction();
                updateSynthLabel();
            });
            Increase_ALBtn = initButton(100, 120, 60, 30, "Inc", container, event -> {
                amendFrequencyAction(F_INC);
            });
            Decrease_ALBtn = initButton(180, 120, 60, 30, "Dec", container, event -> {
                amendFrequencyAction(-F_INC);
            });

            // 4th row - attenuation level
            attnValueLabel = initLabel(35, 170, 200, 40, bigFont, "unknown", container);
            
            // 5th row - enter, inc & dec buttons for attenuation
            initButton(20, 240, 60, 60, "Enter", container, event -> {
                enterAction();
                updateAttenuationLabel();
            });

            initButton(110, 240, 30, 20, "^", container, event -> amendAttenuationAction(A_INC));
            initButton(160, 240, 30, 20, "^", container, event -> amendAttenuationAction(A_INCx));
            initButton(210, 240, 30, 20, "^", container, event -> amendAttenuationAction(A_INCxx));
            initLabel(88, 260, 150, 20, smallFont, "      0.25       1.0        10", container);
            initButton(110, 280, 30, 20, "v", container, event -> amendAttenuationAction(-A_INC));
            initButton(160, 280, 30, 20, "v", container, event -> amendAttenuationAction(-A_INCx));
            initButton(210, 280, 30, 20, "v", container, event -> amendAttenuationAction(-A_INCxx));

            // 6th row - oscillator lock
            synthLockLed = initLed(LED2_y, Color.BLACK, container);
            initLabel(100, 325, 120, 20, smallFont, "Osc. Lock Detected", container);

            // 7th row - PSU OK
            psuAlarmLed = initLed(LED3_y, Color.GREEN, container);
            initLabel(100, 375, 100, 20, smallFont, "PSU Healthy", container);

            // 8th row - button to update data from mbed
            initButton(20, 420, 220, 30, "Update Connection Data", container, event -> get_data());

            // 9th row - IP address
            initLabel(40, 470, 70, 20, smallFont, "IP Address", container);
            ipAddrField.setBounds(110, 470, 120, 20);
            container.add(ipAddrField);

            // 10 row - IP mask
            initLabel(40, 500, 70, 20, smallFont, "IP Mask", container);
            ipMaskField.setBounds(110, 500, 120, 20);
            container.add(ipMaskField);

            // 11th row - update IP button (with action)
            ipSet = initButton(160, 530, 60, 20, "Update IP", container, event -> {
                try {
                    int addr = ipIntFromTextField(ipAddrField);
                    int mask = ipIntFromTextField(ipMaskField);

                    ipAddrField.setText("IP address updating");
                    ipMaskField.setText("IP mask updating");
                    ipErrorLabel.setText("");
                    try {
                        Thread.sleep(1000);  // give time for repaint
                    } catch (InterruptedException x) {
                        System.out.println("This is surprising... Thread.sleep() raised InterruptedException");
                        Thread.currentThread().interrupt();
                    }

                    ipAddrRpc.write(addr);
                    ipMaskRpc.write(mask);
                    CtrlAction.write(7); // notify IP change
                } catch (IllegalArgumentException x) {
                    ipErrorLabel.setText(x.getMessage());
                }
            });

            // 12th row - place for error text from IP update to display
            ipErrorLabel = initLabel(20, 560, 220, 20, smallFont, "", container);

            // Label outside container showing comms count
            connectionCounterLabel = initLabel(270, 570, 30, 20, smallFont, "", this);
            connectionCounterLabel.setForeground(Color.GRAY);
            // ======== end of UI layout ========

            
            SynthFrequency = SynthFrequencyActual.read_int();
            Attenuation = AttenuatorActual.read_int();
            get_data();

            // get limits on user entered frequency from mbed (no longer using SynthType_i)
            SYNTH_FREQ_MIN = minFrequencyMHz.read_int();
            SYNTH_FREQ_MAX = maxFrequencyMHz.read_int();

            System.out.format("Synth frequency: actual = %d, limits: min = %d  max = %d\n",
                            SynthFrequency, SYNTH_FREQ_MIN, SYNTH_FREQ_MAX);
        } else {
            comms_active = 0;
            
            // All is not well with the world
            initLabel(50, 80, 120, 20, smallFont, "Connection Error:", container);
            initLabel(50, 100, 150, 20, smallFont, "Comms Already In Use", container);
        }
    }

    // **************************************************************************
    // * functions for timer and memory control
    // *
    @Override
    public void start() {
        if (comms_active == 1) {
            refresh_timer.start();
        }
    }

    @Override
    public void stop() {
        if (comms_active == 1) {
            CtrlAction.write(0x02); // 01=Set Remote Comms off
            refresh_timer.stop();
        }
        mbed.delete();
        super.destroy();
    }

    @Override
    public void destroy() {
        if (comms_active == 1) {
            CtrlAction.write(0x02); // 01=Set Remote Comms off
        }
        super.destroy();
        mbed.delete();

    }

    // **************************************************************************
    // * function to get data from mbed RPC variable
    // *
    public void get_data() {

        int LEDStatus_i = LEDStatus.read_int();

        ploOscAlarm = ((LEDStatus_i >> 1) & 0x00000001) != 0;
        SynthLockLED_i = ((LEDStatus_i >> 2) & 0x00000001);
        frontPanelControlled = (LEDStatus_i & 0x10) != 0;
        isPLO = (LEDStatus_i & 0x20) != 0;
        PSU1Alarm_i = ((LEDStatus_i >> 7) & 0x00000001);
        SynthType_i = ((LEDStatus_i >> 9) & 0x00000001);
        AttType_i = ((LEDStatus_i >> 10) & 0x00000001);
        SerialAlarm_i = ((LEDStatus_i >> 11) & 0x00000001);

        // 'Light the LED' orange if web UI has control
        localRemoteLed.setLedColor(frontPanelControlled ? Color.white : Color.orange);
        
        // 'Light the LED' good or bad
        boolean isAlarm = (!isPLO && SynthLockLED_i == 0) || (isPLO && ploOscAlarm);
        Color c = isAlarm ? Color.red : Color.green;
        synthLockLed.setBorderColor(c);
        synthLockLed.setLedColor(c);
        
        // 'Light the LED' green if the PSU is OK
        psuAlarmLed.setLedColor(PSU1Alarm_i == 0 ? Color.green : Color.white);

        if (frontPanelControlled) {
            SynthFrequency = SynthFrequencyActual.read_int();
            Attenuation = AttenuatorActual.read_int();
        }

        updateSynthLabel();
        
        Enter_ALBtn.setVisible(!isPLO);
        Increase_ALBtn.setVisible(!isPLO);
        Decrease_ALBtn.setVisible(!isPLO);

        attenuationMax = AttType_i < 1 ? 127 : 255;

        updateAttenuationLabel();
        
        ipAddrField.setEnabled(!frontPanelControlled);
        ipMaskField.setEnabled(!frontPanelControlled);
        ipSet.setEnabled(!frontPanelControlled);
        setIpTextField(ipAddrField, ipAddrRpc);
        setIpTextField(ipMaskField, ipMaskRpc);

        System.out.format("LEDStatus %04X SynthLockLED_i %d, frontPanelControlled %s, PSU1Alarm_i %d, SynthType_i %d, AttType_i %d, SerialAlarm_i %d\n" +
                "SynthFrequency %d, Attenuation %d, attenuationMax %d\n", 
                LEDStatus_i, SynthLockLED_i, Boolean.toString(frontPanelControlled), PSU1Alarm_i, SynthType_i, AttType_i, SerialAlarm_i,
                SynthFrequency, Attenuation, attenuationMax);
    }

    // **************************************************************************
    // * function to be called for each refresh iteration
    // *
    ActionListener timerListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ev) {

            connection_ctr = connection_ctr + 1;
            if (connection_ctr >= 999) {
                connection_ctr = 0;
            }
            connectionCounterLabel.setText(String.valueOf(connection_ctr));
            
            // get_data();
            if ((FreqUpdateIcon == 1) | (AttUpdateIcon == 1)) {
                update_ctr = update_ctr + 1;
                if (update_ctr > 5) {
                    FreqUpdateIcon = 0;
                    SynthFrequency = SynthFrequencyActual.read_int();
                    AttUpdateIcon = 0;
                    Attenuation = AttenuatorActual.read_int();
                    update_ctr = 0;
                }
            }
        }
    };

    // Factory methods to facilitate creation & initialisation of label, buttons & 'LED's
    private Label initLabel(int x, int y, int width, int height, Font font, String text, Container container) {
        Label label = new Label(text);
        label.setFont(font);
        label.setBounds(x, y, width, height);
        container.add(label);
        return label;
    }

    private Button initButton(int x, int y, int width, int height, String name, Container container, ActionListener listener) {
        Button button = new Button(name);
        button.setBounds(x, y, width, height);
        container.add(button);
        button.addActionListener(listener);
        return button;
    }

    private LedPanel initLed(int y, Color color, Container container) {
        LedPanel panel = new LedPanel();
        panel.setRadius(LED_r);
        panel.setBounds(LED_x, y, LED_dx, LED_dy);
        panel.setBorderColor(color);
        container.add(panel);
        return panel;
    }

    // 'Actions' for updating attenuation & frequency values + user pressing enter
    private void amendAttenuationAction(int delta) {
        if (frontPanelControlled)
            return;
        
        Attenuation = Math.max(ATT_MIN, Math.min(attenuationMax, Attenuation + delta));
        AttUpdateIcon = 1;
        update_ctr = 0;
        updateAttenuationLabel();
    }
    
    private void amendFrequencyAction(int delta) {
        if (frontPanelControlled)
            return;
        
        SynthFrequency = Math.max(SYNTH_FREQ_MIN, Math.min(SYNTH_FREQ_MAX, SynthFrequency + delta));
        FreqUpdateIcon = 1;
        update_ctr = 0;
        updateSynthLabel();
    }
    
    private void enterAction() {
        if (frontPanelControlled)
            return;
        
        SynthFrequencyUpdate.write(SynthFrequency);
        AttenuatorUpdate.write(Attenuation);
        CtrlAction.write(0x05); // Enter on
        CtrlAction.write(0x06); // Enter off
        AttUpdateIcon = 0;
        FreqUpdateIcon = 0;
        update_ctr = 0;
        get_data();
    }

    // Update synth & attenuation label text 
    private void updateSynthLabel() {
        synthValueLabel.setText(SerialAlarm_i != 0 ?
                "Synth ALM" :
                String.format("%4d  MHz %s", SynthFrequency, FreqUpdateIcon >= 1 ? "X" : ""));
    }

    private void updateAttenuationLabel() {
        attnValueLabel.setText(
                String.format("%6.2f  dB %s", Attenuation * 0.25, AttUpdateIcon >= 1 ? "X" : ""));
    }

    // Methods for converting to/from IP address/mask string/int 
    private void setIpTextField(TextField ipField, RemoteFactory.Integer ipRpc) {
        // NOTE: Byte order for char[4] read from mbed
        int value = ipRpc.read_int();
        int a = value >>  0 & 0xFF;
        int b = value >>  8 & 0xFF;
        int c = value >> 16 & 0xFF;
        int d = value >> 24 & 0xFF;
        ipField.setText(String.format("%d.%d.%d.%d", a, b, c, d));
    }

    protected int ipIntFromTextField(TextField ipField) throws IllegalArgumentException {
        // NOTE: Byte order for char[4] written to mbed
        String text = ipField.getText();
        String[] split = text.split("\\.");
        if (split.length != 4) {
            throw new IllegalArgumentException("Expected 4 numbers in " + text);
        }
        int a = parseInt(text, split[0]);
        int b = parseInt(text, split[1]);
        int c = parseInt(text, split[2]);
        int d = parseInt(text, split[3]);

        int value =
                 (a & 0xFF) <<  0;
        value |= (b & 0xFF) <<  8;
        value |= (c & 0xFF) << 16;
        value |= (d & 0xFF) << 24;

        return value;
    }

    private int parseInt(String whole, String value) {
        try {
            int result = Integer.parseInt(value);
            if (result < 0 || result > 255) {
                throw new IllegalArgumentException(
                        String.format("Invalid number %s in %s", value, whole));
            }
            return result;
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException(
                    String.format("Invalid number %s in %s", value, whole));
        }
    }
}
