package jm;
import robocode.*;
import java.awt.Color;
import robocode.ScannedRobotEvent;
import java.util.Random;
import java.util.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.geom.Point2D;

public class TecnoTN extends AdvancedRobot
{
	private AdvancedEnemyBot enemy = new AdvancedEnemyBot();
	private byte moveDirection = 1;
	private byte scanDirection = 1;
	/**
	 * run: TecnoTN's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here
		setBodyColor(Color.black);
		setGunColor(Color.red);
		setRadarColor(Color.yellow);
		setBulletColor(Color.red);
		setScanColor(Color.green);	
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		enemy.reset();
	
		while(true) {
			setTurnRadarRight(360);
			avoidWall();
			doMove();
			execute();
		}
	}

	private double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;
	
		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}
	
		return bearing;
	}

	public void doMove() {
		if (getVelocity() == 0)
			moveDirection *= -1;
	
		// always square off against our enemy
		setTurnRight(enemy.getBearing() + 90);
	
		// strafe by changing direction every 20 ticks
		if (getTime() % 20 == 0) {
			moveDirection *= -1;
			setAhead(150 * moveDirection);
		}
	}

	private double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (e.getDistance() < 250) {
			if (enemy.none()) {
				enemy.update(e, this);
			} else if (e.getEnergy() < enemy.getEnergy()) {
				enemy.update(e, this);	
			} else if (e.getDistance() < enemy.getDistance()) {
				enemy.update(e, this);
			}
		} 
		
		if (enemy.none() && e.getDistance() <= 600) {
			enemy.update(e, this);
		}
		
		scanDirection *= -1; // changes value from 1 to -1
		setTurnRadarRight(360 * scanDirection);
			
		double firePower = Math.min(350 / enemy.getDistance(), 3);
		double bulletSpeed = 20 - firePower * 3;
		long time = (long)(enemy.getDistance() / bulletSpeed);
			
		double futureX = enemy.getFutureX(time);
		double futureY = enemy.getFutureY(time);
		double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);

		setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)
			setFire(firePower);
	}
	
	/**
	 * onRobotDeath: What to do when you see another robot die
	 */
	public void onRobotDeath(RobotDeathEvent e) {
		if (e.getName().equals(enemy.getName())) {
			enemy.reset();
		}
	}
	
	private boolean avoidWall() {
        if( getBattleFieldHeight() - getY() < 300 || getY() < 300 ||
            getBattleFieldWidth() - getX() < 300 || getX() < 300) {
                turnRight(90);
				ahead(200);
                return true;
            }
        return false;
    }	
}

class EnemyBot {
 double bearing;
	double distance;
	double energy;
	double heading;
	double velocity;
	String name;
	
	public double getBearing(){
		return bearing;		
	}
	public double getDistance(){
		return distance;
	}
	public double getEnergy(){
		return energy;
	}
	public double getHeading(){
		return heading;
	}
	public double getVelocity(){
		return velocity;
	}
	public String getName(){
		return name;
	}
	public void update(ScannedRobotEvent bot){
		bearing = bot.getBearing();
		distance = bot.getDistance();
		energy = bot.getEnergy();
		heading = bot.getHeading();
		velocity = bot.getVelocity();
		name = bot.getName();
	}
	public void reset(){
		bearing = 0.0;
		distance =0.0;
		energy= 0.0;
		heading =0.0;
		velocity = 0.0;
		name = null;
	}
	
	public Boolean none(){
		if (name == null || name == "")
			return true;
		else
			return false;
	}
	
	public EnemyBot(){
		reset();
	}

}

class AdvancedEnemyBot extends EnemyBot{
	private double x, y;
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public void reset(){
		super.reset();
		x = 0;
		y = 0;
	}
	
	public AdvancedEnemyBot(){
		reset();
	}
	
	public void update(ScannedRobotEvent e, Robot robot){
		super.update(e);
		double absBearingDeg= (robot.getHeading() + e.getBearing());
		if (absBearingDeg <0) absBearingDeg +=360;
		
		x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();
		y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
		
	}
	
	public double getFutureX(long when){
		return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
	}
	
	public double getFutureY(long when ){
		return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
	}
}
