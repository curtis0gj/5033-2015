package org.usfirst.frc.team5033.robot;


import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Robot extends SampleRobot {
    RobotDrive robot;
    Joystick stick;
    Joystick xbox;
    Encoder encoder;	
    Gyro gyro;
    Relay leftarmwheel;
    Relay rightarmwheel;
    Victor screwmotor1;
    Victor screwmotor2;
    Victor armmotor;
    DigitalInput limit;
    DigitalInput limit2;
    DigitalInput limit3;
    DigitalInput limit4;
    double Kp = 0.01;
    boolean bottomscrewlimit = false;
    boolean limitPressed2 = false;
    boolean minarmlimit = false;
    boolean maxarmlimit = false;
    double x = 0;
    SendableChooser autoChooser;
    Defines.AUTOS autoMethod;

    public Robot() {
    	robot = new RobotDrive(0, 1);
        stick = new Joystick(1);
        xbox = new Joystick(0);
        gyro = new Gyro(0);
        encoder = new Encoder(0, 1, false, EncodingType.k4X);
        leftarmwheel = new Relay(1);
        rightarmwheel = new Relay(2);
        limit = new DigitalInput(4);	
        limit2 = new DigitalInput(5);
        limit3 = new DigitalInput(6);
        limit4 = new DigitalInput(7);
        screwmotor1 = new Victor(4);
        screwmotor2 = new Victor(5);
        armmotor = new Victor(6);
        gyro.reset();
        gyro.initGyro();
        encoder.setDistancePerPulse(0.3193143);
        encoder.getDistance();
        
        autoChooser = new SendableChooser();
        autoChooser.addDefault("Pick Up Two", Defines.AUTOS.AUTO_TWO);
        autoChooser.addObject("Pick Up One", Defines.AUTOS.AUTO_ONE);
        autoChooser.addObject("Move Into Autozone", Defines.AUTOS.AUTO_MOVE);
        autoChooser.addObject("Do Nothing", Defines.AUTOS.AUTO_NOTHING);
        SmartDashboard.putData("Auto mode", autoChooser);
    }

    public void autonomous() {
    	autoMethod = (Defines.AUTOS)autoChooser.getSelected();

    	if(autoMethod == Defines.AUTOS.AUTO_ONE) 
    		return;
    	if(autoMethod == Defines.AUTOS.AUTO_NOTHING)
    		return;
    	if(autoMethod == Defines.AUTOS.AUTO_MOVE) {
    		while(true) {
    			//Drive to auto zone.
        		double distance = encoder.get();
        		double angle = gyro.getAngle();
    			if(!isAutonomous() || !isEnabled())
    				return;
    			robot.drive(-0.25, angle * Kp);
	    		if(distance < -3000) {
	    			robot.drive(0, 0);
	    			return;
	    		}
    		}
    	}
		
    	double ScrewTime = Timer.getFPGATimestamp();
    	while(true)  {
    		//Lift garbage can for 3 seconds and stop, wait one second.
    		if(!isAutonomous() || !isEnabled())
    			return;
    		if(ScrewTime + 3 > Timer.getFPGATimestamp()) {
    			screwmotor1.set(Defines.SCREW_SPEED);
    			screwmotor2.set(Defines.SCREW_SPEED);
    		} else {
    			break;
    		}
    	}

    	screwmotor1.set(Defines.SCREW_OFF);
    	screwmotor2.set(Defines.SCREW_OFF);
    	Timer.delay(Defines.AUTO_TIMEBREAKS); 

    	while(true) {
    		//Travel to the bin, stop, reset sensors and wait one second.
    		double angle = gyro.getAngle();
    		double distance = encoder.get();
    		if(!isAutonomous() || !isEnabled())
    			return;
    		robot.drive(-.25, angle * Kp);
    		if(distance < -100) {
    			break;
    		}
    	}

    	robot.drive(0, 0);
    	encoder.reset();
    	gyro.reset();
    	Timer.delay(Defines.AUTO_TIMEBREAKS); // again, what if auto ends?

    	double ArmTime = Timer.getFPGATimestamp();
    	while(true) {
    		//Close arms for two seconds and stop, wait one.
    		// || && maxarmlimit = limit4.get();
    		if(!isAutonomous() || !isEnabled())
    			return;
    		maxarmlimit = limit4.get();
    		if(ArmTime + 2 > Timer.getFPGATimestamp() && maxarmlimit == true) {
    			armmotor.set(Defines.ARM_SPEED);
    		} else {
    			break;
    		}
    	}

    	armmotor.set(0);
    	encoder.reset();
    	Timer.delay(Defines.AUTO_TIMEBREAKS);

    	while(true) {
    		//Turn 90, stop and wait one second.
    		double angle = gyro.getAngle();
    		if(!isAutonomous() || !isEnabled())
    			return;
    		robot.drive(-.40, 1);
    		if(angle < 90) {
    			break;
    		}
    	}

    	robot.drive(0,0);
    	encoder.reset();
    	Timer.delay(Defines.AUTO_TIMEBREAKS);

    	while(true) {
    		//Drive forward until distance is met and stop.
    		double angle = gyro.getAngle();
    		double distance = encoder.get();
    		if(!isAutonomous() || !isEnabled())
    			return;
    		robot.drive(-.25, angle * Kp);
    		if(distance < -500) {
    			robot.drive(0, 0);
    			break;
    		}
    	}
    }
        		
    public void operatorControl() {

    	while (isOperatorControl() && isEnabled()) {
    	    double throttle = stick.getRawAxis(Defines.STICK_THROTTLE);
       	    double leftaxis = xbox.getRawAxis(Defines.LEFT_AXIS);
    	    double rightaxis = xbox.getRawAxis(Defines.RIGHT_AXIS);
            bottomscrewlimit = limit.get(); 
            minarmlimit = limit3.get();
            maxarmlimit = limit4.get();
    	    x = (-throttle + 1) / 2;
    		
            robot.arcadeDrive(stick.getY() * x, -stick.getX() * x);

            //limitPressed2 = limit2.get();
            /*double angle = gyro.getAngle();
    	      SmartDashboard.putNumber("angle", angle);
    	      double distance = encoder.getDistance();
    	      SmartDashboard.putNumber("distance", distance);*/
            //System.out.println("limitPressed=" + limitPressed); //Read the RoboRIO log for some values.
            //System.out.println("limitPressed2=" + limitPressed2);
            //System.out.println("limitPressed3=" + limitPressed3);
            //System.out.println("limitPressd4=" + limitPressed4);
            
            if(-leftaxis > 0.5) { // && limitPressed2 == true
            	screwmotor1.set(Defines.SCREW_SPEED);
            	screwmotor2.set(Defines.SCREW_SPEED);
            } else if(leftaxis > 0.5 && bottomscrewlimit == false) { 
            	screwmotor1.set(-Defines.SCREW_SPEED);
            	screwmotor2.set(-Defines.SCREW_SPEED);
            } else {
            	screwmotor1.set(Defines.SCREW_OFF);
            	screwmotor2.set(Defines.SCREW_OFF);
            }
            
            if(-rightaxis > 0.5 && maxarmlimit == true) { 
            	armmotor.set(Defines.ARM_SPEED);
            } else if(rightaxis > 0.5 && minarmlimit == false) {
            	armmotor.set(-Defines.ARM_SPEED);
            } else {
            	armmotor.set(Defines.ARM_OFF);
            }
            
            if(xbox.getRawButton(Defines.RIGHT_TRIGGER)) { 
            	leftarmwheel.set(Relay.Value.kForward);
            	rightarmwheel.set(Relay.Value.kReverse);
            } else if(xbox.getRawButton(Defines.LEFT_TRIGGER)) { 
            	leftarmwheel.set(Relay.Value.kReverse);
            	rightarmwheel.set(Relay.Value.kForward);     
            } else {
            	leftarmwheel.set(Relay.Value.kOff);
            	rightarmwheel.set(Relay.Value.kOff);            	
            }
    	}
    }
            
    public void test() {
    	
    }
}
