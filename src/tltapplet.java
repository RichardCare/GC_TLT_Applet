import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.*;
import java.lang.Math.*;
import javax.swing.Timer;

import org.mbed.RPC.*;

public class tltapplet extends Applet implements mbedRPC, ActionListener {

	HTTPRPC mbed;
	boolean threadSuspended;
	Timer refresh_timer;
	private static final long serialVersionUID = 1L;
        
        // setup local and rpc variables
//        RPCVariable<Integer> Open;
//      RPCVariable<Integer> LEDStatus;
      RPCVariable<Integer> CtrlAction;
      RPCVariable<Character> LEDStatus0;
      RPCVariable<Character> LEDStatus1;
        RPCVariable<Integer> SynthFrequencyActual;
        RPCVariable<Integer> SynthFrequencyUpdate;
        RPCVariable<Integer> AttenuatorActual;
        RPCVariable<Integer> AttenuatorUpdate;

//        RPCVariable<Integer> LocalActiveBtn;
//        RPCVariable<Integer> EnterBtn;
        //RPCVariable<Integer> IncreaseBtn;
        //RPCVariable<Integer> DecreaseBtn;
//        RPCVariable<Integer> PLO1PowerBtn;
//        RPCVariable<Integer> PLO2PowerBtn;
//        RPCVariable<Integer> SynthMuteBtn;

        // screen position copordinates for drawing LEDs, Btns and text
        int LED1_x=20;
        int LED2_x=80;
        int LED3_x=140;
        int LED4_x=200;
        int LED1_y=21;
        int LED2_y=191;
        int LED3_y=241;
        int LED4_y=271;
        int LED5_y=321;
        int LED6_y=371;
        int LED_dx=40;
        int LED_dy=28;
        int LED_r=6;

//        int OpenStatus=0;
//        int LEDStatus_i=0;
        int LEDStatus0_i=0;
        int LEDStatus1_i=0;
        int LocalActiveLED_i=0;
//        int PLO1LED_i=0;
//        int PLO2LED_i=0;
//        int SynthMuteLED_i=0;
        int SynthLockLED_i=0;

//        int PLO1Alarm_i=0;
//        int PLO2Alarm_i=0;
        int PSU1Alarm_i=0;
//        int PSU2Alarm_i=0;
        int SynthType_i=0;
        int AttType_i=0;
        int SerialAlarm_i=0;

	int SynthFrequency;
        int FreqUpdateIcon=0;        // displayed if the frequency has been changed
        int F_INC=25;                  // increment value in MHz
        int SYNTH_FREQ_MIN;
        int SYNTH_FREQ_MAX;

	int Attenuation;
        int AttUpdateIcon=0;        // displayed if the attenuator has been changed
        int A_INC=1;                  // increment value in bits
        int A_INCx=4;                  // increment value in bits
        int A_INCxx=40;                  // increment value in bits
        int ATT_MIN=0;
        int ATT_MAX;

        int CtrlStatusData=0;
        int CommsOpenFlag=0;
        int comms_active=0;
        int connection_ctr=0;
        int update_ctr=0;
        
//        Button Inactive_ALBtn;

	Button LocalActive_ALBtn;
	Button Enter_ALBtn;
	Button Enter2_ALBtn;
	Button Increase_ALBtn;
	Button Decrease_ALBtn;
	Button AttInc_ALBtn;
	Button AttDec_ALBtn;
	Button AttIncx_ALBtn;
	Button AttDecx_ALBtn;
	Button AttIncxx_ALBtn;
	Button AttDecxx_ALBtn;
	Button Refresh_ALBtn;

//	Button PLO1_ALBtn;
//	Button PLO2_ALBtn;
	//Button SynthMute_ALBtn;

        // **************************************************************************     
        // * function to initialise
        // *
	public void init() {

                setLayout(null);

		mbed = new HTTPRPC(this); 

              LEDStatus1 = new RPCVariable<Character>(mbed,"RemoteLEDStatus1");   // wont work with bool
              LEDStatus1_i=LEDStatus1.read_char();             

              CommsOpenFlag=((LEDStatus1_i>>4) & 0x01);

              if (CommsOpenFlag==0){
                CtrlAction = new RPCVariable<Integer>(mbed,"RemoteCtrlAction");          
                CtrlAction.write(0x01);          //01=Set Remote Comms Open/Active        
                comms_active=1;
            
                //LEDStatus = new RPCVariable<Integer>(mbed,"RemoteLEDStatus");
                LEDStatus0 = new RPCVariable<Character>(mbed,"RemoteLEDStatus0");          
                SynthFrequencyActual = new RPCVariable<Integer>(mbed,"RemoteSynthFrequencyActual");
                SynthFrequencyUpdate = new RPCVariable<Integer>(mbed,"RemoteSynthFrequencyUpdate");
                AttenuatorActual = new RPCVariable<Integer>(mbed,"RemoteAttenuatorActual");
                AttenuatorUpdate = new RPCVariable<Integer>(mbed,"RemoteAttenuatorUpdate");

                //LocalActiveBtn = new RPCVariable<Integer>(mbed,"RemoteLocalActiveBtn");
                //EnterBtn = new RPCVariable<Integer>(mbed,"RemoteEnterBtn");
                //IncreaseBtn = new RPCVariable<Integer>(mbed,"RemoteIncreaseBtn");
                //DecreaseBtn = new RPCVariable<Integer>(mbed,"RemoteDecreaseBtn");                
//                PLO1PowerBtn = new RPCVariable<Integer>(mbed,"RemotePLO1PowerBtn");
//                PLO2PowerBtn = new RPCVariable<Integer>(mbed,"RemotePLO2PowerBtn");
                //SynthMuteBtn = new RPCVariable<Integer>(mbed,"RemoteSynthMuteBtn");

 		int rate; 
		try{
			rate =Integer.parseInt(getParameter("rate"));
		}catch(Exception e){
			System.err.println("No parameter found");
			rate = 1000;
		}
		refresh_timer = new Timer(rate, timerListener);
		refresh_timer.start();


                LocalActive_ALBtn    = new Button("Local / Remote");
                Enter_ALBtn = new Button("Enter");
                Enter2_ALBtn = new Button("Enter");
                Increase_ALBtn   = new Button("Inc");
                Decrease_ALBtn   = new Button("Dec");
                AttInc_ALBtn   = new Button("^");
                AttDec_ALBtn   = new Button("v");
                AttIncx_ALBtn   = new Button("^");
                AttDecx_ALBtn   = new Button("v");
                AttIncxx_ALBtn   = new Button("^");
                AttDecxx_ALBtn   = new Button("v");
                Refresh_ALBtn   = new Button("Update Connection Data");


//                PLO1_ALBtn  = new Button("PLO1 - 10050 MHz");
//                PLO2_ALBtn  = new Button("PLO2 - 10950 MHz");
                //SynthMute_ALBtn  = new Button("Synth Mute");

                LocalActive_ALBtn.setBounds(80,20,160,30);
                Enter_ALBtn.setBounds(20,120,60,30);
                Enter2_ALBtn.setBounds(20,240,60,60);
                Increase_ALBtn.setBounds(100,120,60,30);
                Decrease_ALBtn.setBounds(180,120,60,30);
                AttInc_ALBtn.setBounds(110,240,30,20);
                AttDec_ALBtn.setBounds(110,280,30,20);
                AttIncx_ALBtn.setBounds(160,240,30,20);
                AttDecx_ALBtn.setBounds(160,280,30,20);
                AttIncxx_ALBtn.setBounds(210,240,30,20);
                AttDecxx_ALBtn.setBounds(210,280,30,20);
                Refresh_ALBtn.setBounds(20,420,220,30);
//                PLO1_ALBtn.setBounds(80,190,160,30);
//                PLO2_ALBtn.setBounds(80,240,160,30);
                //SynthMute_ALBtn.setBounds(80,270,160,30);

                add(LocalActive_ALBtn);
                add(Enter_ALBtn);
                add(Enter2_ALBtn);
                add(Increase_ALBtn);
                add(Decrease_ALBtn);
                add(AttInc_ALBtn);
                add(AttDec_ALBtn);
                add(AttIncx_ALBtn);
                add(AttDecx_ALBtn);
                add(AttIncxx_ALBtn);
                add(AttDecxx_ALBtn);
                add(Refresh_ALBtn);

//                add(PLO1_ALBtn);
//                add(PLO2_ALBtn);
                //add(SynthMute_ALBtn);

                LocalActive_ALBtn.addActionListener(this); 
                Enter_ALBtn.addActionListener(this);
                Enter2_ALBtn.addActionListener(this);
                Increase_ALBtn.addActionListener(this); 
                Decrease_ALBtn.addActionListener(this); 
                AttInc_ALBtn.addActionListener(this); 
                AttDec_ALBtn.addActionListener(this); 
                AttIncx_ALBtn.addActionListener(this); 
                AttDecx_ALBtn.addActionListener(this); 
                AttIncxx_ALBtn.addActionListener(this); 
                AttDecxx_ALBtn.addActionListener(this); 
                Refresh_ALBtn.addActionListener(this); 
//                PLO1_ALBtn.addActionListener(this); 
//                PLO2_ALBtn.addActionListener(this); 
                //SynthMute_ALBtn.addActionListener(this);  

 		get_data();
                SynthFrequency=SynthFrequencyActual.read_int();
                Attenuation=AttenuatorActual.read_int();

                if (SynthType_i<1){
                    SYNTH_FREQ_MIN=1500;
                    SYNTH_FREQ_MAX=3300;
                }else{
                    SYNTH_FREQ_MIN=8000;
                    SYNTH_FREQ_MAX=13100;
                }
                if (AttType_i<1){
                    ATT_MAX=127;
                }else{
                    ATT_MAX=255;
                }
              
              }else{
                  comms_active=0;
              }

	}
        
        // **************************************************************************     
        // * functions for timer and memory control
        // *
	public void start(){
                if(comms_active==1){
        	    refresh_timer.start();	
	        }	
	}
	
	public void stop(){
                if(comms_active==1){
                    CtrlAction.write(0x02);          //01=Set Remote Comms off
	            refresh_timer.stop();
                }
                mbed.delete();
                super.destroy();
	}

	public void destroy(){
                if(comms_active==1){
                    CtrlAction.write(0x02);          //01=Set Remote Comms off           
                }
                super.destroy();
                mbed.delete();
                
	}

        // **************************************************************************     
        // * function to get data from mbed RPC variable
        // *
        public void get_data(){        


               LEDStatus0_i=LEDStatus0.read_char();
               LEDStatus1_i=LEDStatus1.read_char();                         

               SynthLockLED_i     = ((LEDStatus0_i>>2) & 0x00000001);
               LocalActiveLED_i   = ((LEDStatus0_i>>4) & 0x00000001);
               PSU1Alarm_i        = ((LEDStatus0_i>>7) & 0x00000001);
               SynthType_i        = ((LEDStatus1_i>>1) & 0x00000001);
               AttType_i          = ((LEDStatus1_i>>2) & 0x00000001);
               SerialAlarm_i      = ((LEDStatus1_i>>3) & 0x00000001);

               if (LocalActiveLED_i>=1){
                   SynthFrequency=SynthFrequencyActual.read_int();
                   Attenuation=AttenuatorActual.read_int();
               }
               
        }

        // **************************************************************************     
        // * function to be called for each refresh iteration
        // *
	ActionListener timerListener = new ActionListener() {
                public void actionPerformed(ActionEvent ev) {

                            connection_ctr=connection_ctr+1;
                            if (connection_ctr>=999){
                                    connection_ctr=0;
                            }
                            //get_data();
                            if ((FreqUpdateIcon==1)|(AttUpdateIcon==1)){
                               update_ctr=update_ctr+1;
                               if (update_ctr>5){
                                   FreqUpdateIcon=0;
                                   SynthFrequency=SynthFrequencyActual.read_int();
                                   AttUpdateIcon=0;
                                   Attenuation=AttenuatorActual.read_int();
                                   update_ctr=0;
                               }
                            }
			    repaint();

		}
        };
 


        // **************************************************************************     
        // * function to setup graphics and paint (empty)
        // *
        public void paint(Graphics g) { 

                g.setColor(Color.blue);
                g.drawRoundRect(1, 1, 260, 460, 20, 20);

                Font smallFont = new Font("Arial",Font.PLAIN,13);
                Font bigFont = new Font("Arial",Font.PLAIN,32);

              if (comms_active==1){

                g.setFont(smallFont);
                g.setColor(Color.black);
                g.drawString("      0.25       1.0        10",90,275);
                g.drawString("Synth Lock Detected",100,342);
                g.drawString("PSU Healthy",100,391);

                g.setFont(bigFont);
                g.setColor(Color.black);
                if (SerialAlarm_i>=1){
                    g.drawString("Synth ALM",35,96);
                }else{
                    g.drawString(String.valueOf(SynthFrequency),35,96);
                    g.drawString("MHz",140,96);
                    if (FreqUpdateIcon >= 1){
                       g.drawString("X",225,96);
                    }
                }

                g.drawString(String.valueOf(Attenuation*0.25),35,216);
                g.drawString("dB",140,216);
                if (AttUpdateIcon >= 1){
                   g.drawString("X",225,216);
                }   

                // Draw Local/Remote LED and fill if active
	        if(LocalActiveLED_i <= 0){	
	                g.setColor(Color.orange);	  
	        }else{
        	        g.setColor(Color.white);
	        }  
        	g.fillRoundRect(LED1_x, LED1_y, LED_dx, LED_dy, LED_r, LED_r);
	        g.setColor(Color.orange);
        	g.drawRoundRect(LED1_x, LED1_y, LED_dx, LED_dy, LED_r, LED_r); 

                // Draw PLO1 LED and fill if active
//	        if(PLO1LED_i >= 1){	
//	                g.setColor(Color.green);	  
//	        }else{
//        	        g.setColor(Color.white);
//	        }  
//        	g.fillRoundRect(LED1_x, LED2_y, LED_dx, LED_dy, LED_r, LED_r);
//	        g.setColor(Color.green);
//        	g.drawRoundRect(LED1_x, LED2_y, LED_dx, LED_dy, LED_r, LED_r); 

                // Draw PLO2 LED and fill if active
//	        if(PLO2LED_i >= 1){	
//	                g.setColor(Color.green);	  
//	        }else{
//        	        g.setColor(Color.white);
//	        }  
//        	g.fillRoundRect(LED1_x, LED3_y, LED_dx, LED_dy, LED_r, LED_r);
//	        g.setColor(Color.green);
//        	g.drawRoundRect(LED1_x, LED3_y, LED_dx, LED_dy, LED_r, LED_r); 

                // Draw Synth Mute LED and fill if active
	        //if(SynthMuteLED_i >= 1){	
	        //        g.setColor(Color.green);	  
	        //}else{
        	//        g.setColor(Color.white);
	        //}  
        	//g.fillRoundRect(LED1_x, LED4_y, LED_dx, LED_dy, LED_r, LED_r);
	        //g.setColor(Color.green);
        	//g.drawRoundRect(LED1_x, LED4_y, LED_dx, LED_dy, LED_r, LED_r); 

                // Draw Synth Lock LED and fill if active
	        if(SynthLockLED_i >= 1){	
	                g.setColor(Color.green);	  
	        }else{
        	        g.setColor(Color.red);
	        }  
        	g.fillRoundRect(LED1_x, LED5_y, LED_dx, LED_dy, LED_r, LED_r);
	        //g.setColor(Color.green);
        	//g.drawRoundRect(LED1_x, LED5_y, LED_dx, LED_dy, LED_r, LED_r); 

                // Draw PSU1 Alarm LED and fill if alive
	        if(PSU1Alarm_i <= 0){	
	                g.setColor(Color.green);	  
	        }else{
        	        g.setColor(Color.white);
	        }  
        	g.fillRoundRect(LED1_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r);
	        g.setColor(Color.green);
        	g.drawRoundRect(LED1_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r); 


                // Draw PSU2 Alarm LED and fill if alive
//	        if(PSU2Alarm_i <= 0){	
//	                g.setColor(Color.green);	  
//	        }else{
//        	        g.setColor(Color.white);
//	        }  
//      	g.fillRoundRect(LED2_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r);
//	        g.setColor(Color.green);
//        	g.drawRoundRect(LED2_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r); 


                // Draw PSU3 Alarm LED and fill if alive
//	        if(PSU3Alarm_i <= 0){	
//	                g.setColor(Color.green);	  
//	        }else{
//        	        g.setColor(Color.white);
//	        }  
//        	g.fillRoundRect(LED3_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r);
//	        g.setColor(Color.green);
//        	g.drawRoundRect(LED3_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r); 


                // Draw PSU4 Alarm LED and fill if alive
//	        if(PSU4Alarm_i <= 0){	
//	                g.setColor(Color.green);	  
//	        }else{
//        	        g.setColor(Color.white);
//	        }  
//        	g.fillRoundRect(LED4_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r);
//	        g.setColor(Color.green);
//        	g.drawRoundRect(LED4_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r); 
                
//                g.setFont(smallFont);
//                g.setColor(Color.black);
//                g.drawString("PSU1",LED1_x+4,LED6_y+20);
//                g.drawString("PSU2",LED2_x+4,LED6_y+20);
//                g.drawString("PSU3",LED3_x+4,LED6_y+20);
//                g.drawString("PSU4",LED4_x+4,LED6_y+20);

//	        if(PLO1Alarm_i >= 1){	
//	                g.drawString("ALM",LED1_x+6,LED2_y+20);
//	        }

//	        if(PLO2Alarm_i >= 1){	
//	                g.drawString("ALM",LED1_x+6,LED3_y+20);
//	        }

                Font tinyFont = new Font("Arial",Font.PLAIN,8);
                g.setColor(Color.gray);
                g.setFont(smallFont);
                g.drawString(String.valueOf(connection_ctr),270,460);


              }else{

                g.setFont(smallFont);
                g.setColor(Color.black);
                g.drawString("Connection Error:",50,80);
                g.drawString("Comms Already In Use",50,100);


              }

}
	
	
     


 // Here we ask which component called this method 

 public void actionPerformed(ActionEvent evt)  
         { 
              if (evt.getSource() == LocalActive_ALBtn)  {
                   CtrlAction.write(0x03);          // LR on
                   CtrlAction.write(0x04);          // LR off
                   get_data();
                   repaint(); 
              }
              
              if (evt.getSource() == Refresh_ALBtn)  {
                   get_data();
                   repaint(); 
              }

              if(LocalActiveLED_i <= 0){

                  if (evt.getSource() == Enter_ALBtn)  {
                       SynthFrequencyUpdate.write(SynthFrequency);
                       AttenuatorUpdate.write(Attenuation);
                       CtrlAction.write(0x05);          // Enter on
                       CtrlAction.write(0x06);          // Enter off
                       FreqUpdateIcon=0;
                       AttUpdateIcon=0;
                       update_ctr=0;
                       get_data();
                       repaint(); 
                  }
                  if (evt.getSource() == Enter2_ALBtn)  {
                       SynthFrequencyUpdate.write(SynthFrequency);
                       AttenuatorUpdate.write(Attenuation);
                       CtrlAction.write(0x05);          // Enter on
                       CtrlAction.write(0x06);          // Enter off
                       AttUpdateIcon=0;
                       FreqUpdateIcon=0;
                       update_ctr=0;
                       get_data();
                       repaint(); 
                  }
                  if (evt.getSource() == Increase_ALBtn)  {
                       SynthFrequency=SynthFrequency+F_INC;
                       if (SynthFrequency>=SYNTH_FREQ_MAX){
                           SynthFrequency=SYNTH_FREQ_MAX;
                       }
                       FreqUpdateIcon=1;
                       update_ctr=0;
	               repaint(); 
                  }              
                  if (evt.getSource() == Decrease_ALBtn)  {
                       SynthFrequency=SynthFrequency-F_INC;
                       if (SynthFrequency<=SYNTH_FREQ_MIN){
                           SynthFrequency=SYNTH_FREQ_MIN;
                       }
                       FreqUpdateIcon=1;
                       update_ctr=0;
		       repaint();
                  }
                  if (evt.getSource() == AttInc_ALBtn)  {
                       Attenuation=Attenuation+A_INC;
                       if (Attenuation>=ATT_MAX){
                           Attenuation=ATT_MAX;
                       }
                       AttUpdateIcon=1;
                       update_ctr=0;
	               repaint(); 
                  }              
                  if (evt.getSource() == AttDec_ALBtn)  {
                       Attenuation=Attenuation-A_INC;
                       if (Attenuation<=ATT_MIN){
                           Attenuation=ATT_MIN;
                       }
                       AttUpdateIcon=1;
                       update_ctr=0;
		       repaint();
                  }
                  if (evt.getSource() == AttIncx_ALBtn)  {
                       Attenuation=Attenuation+A_INCx;
                       if (Attenuation>=ATT_MAX){
                           Attenuation=ATT_MAX;
                       }
                       AttUpdateIcon=1;
                       update_ctr=0;
	               repaint(); 
                  }              
                  if (evt.getSource() == AttDecx_ALBtn)  {
                       Attenuation=Attenuation-A_INCx;
                       if (Attenuation<=ATT_MIN){
                           Attenuation=ATT_MIN;
                       }
                       AttUpdateIcon=1;
                       update_ctr=0;
		       repaint();
                  }
                  if (evt.getSource() == AttIncxx_ALBtn)  {
                       Attenuation=Attenuation+A_INCxx;
                       if (Attenuation>=ATT_MAX){
                           Attenuation=ATT_MAX;
                       }
                       AttUpdateIcon=1;
                       update_ctr=0;
	               repaint(); 
                  }              
                  if (evt.getSource() == AttDecxx_ALBtn)  {
                       Attenuation=Attenuation-A_INCxx;
                       if (Attenuation<=ATT_MIN){
                           Attenuation=ATT_MIN;
                       }
                       AttUpdateIcon=1;
                       update_ctr=0;
		       repaint();
                  }

                  //if (evt.getSource() == PLO1_ALBtn)  {
                  //     PLO1PowerBtn.write(1);
                  //     PLO1PowerBtn.write(0);
                  //}
                  //if (evt.getSource() == PLO2_ALBtn)  {
                  //     PLO2PowerBtn.write(1);
                  //     PLO2PowerBtn.write(0);
                  //}
                  //if (evt.getSource() == SynthMute_ALBtn)  {
                  //     SynthMuteBtn.write(1);
                  //     SynthMuteBtn.write(0);
                  //     repaint(); 
                  //}
              }
 
}


}



