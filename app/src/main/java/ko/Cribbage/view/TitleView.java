package ko.Cribbage.view;

import ko.Cribbage.R;
import ko.Cribbage.activity.CribbageActivity;
import ko.Cribbage.activity.RulesActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class TitleView extends View {
	private Bitmap titleGraphic;
	private Bitmap playButtonUp;
	private Bitmap playButtonDown;
	private Bitmap rulesButtonUp;
	private Bitmap rulesButtonDown;
	private int screenW;
	private int screenH;
	private boolean playButtonPressed;
	private boolean rulesButtonPressed;
	private Context myContext;
	public boolean soundOn = true;
	
	public TitleView(Context context) {
		super(context);
		this.myContext = context;
		titleGraphic = BitmapFactory.decodeResource(getResources(), ko.Cribbage.R.drawable.title);
		titleGraphic = BitmapFactory.decodeResource(getResources(), R.drawable.title);
		playButtonUp = BitmapFactory.decodeResource(getResources(), R.drawable.button_play_up);
		playButtonDown = BitmapFactory.decodeResource(getResources(), R.drawable.button_play_down);
		rulesButtonUp = BitmapFactory.decodeResource(getResources(), R.drawable.button_rules_up);
		rulesButtonDown = BitmapFactory.decodeResource(getResources(), R.drawable.button_rules_down);
	}
	
	public void onDraw(Canvas canvas) {
		canvas.drawBitmap(titleGraphic,(screenW-titleGraphic.getWidth())/2,0,null);
		if (playButtonPressed) {
			canvas.drawBitmap(playButtonDown, (screenW-playButtonUp.getWidth())/2, (int)(screenH*0.6), null);
		} else {
			canvas.drawBitmap(playButtonUp, (screenW-playButtonUp.getWidth())/2, (int)(screenH*0.6), null);
		}
		if (rulesButtonPressed) {
			canvas.drawBitmap(rulesButtonDown, (screenW-rulesButtonDown.getWidth())/2, (int)(screenH*0.6+playButtonUp.getHeight()), null);
		} else {
			canvas.drawBitmap(rulesButtonUp, (screenW-rulesButtonUp.getWidth())/2, (int)(screenH*0.6+playButtonUp.getHeight()), null);
		}
	}
	
    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        screenW = w;
        screenH = h;
        System.out.println("SCREEN W: " + screenW);
        System.out.println("SCREEN H: " + screenH);
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		switch(action) {
			case MotionEvent.ACTION_DOWN:
				//might start drawing from bottom of screen instead.
	        	if (x > (screenW-playButtonUp.getWidth())/2 &&
		        		x < ((screenW-playButtonUp.getWidth())/2) + playButtonUp.getWidth() &&
		        		y > (int)(screenH*0.6) &&
		        		y < (int)(screenH*0.6) + playButtonUp.getHeight()) {
		        		playButtonPressed = true;
				}
				else if(x > (screenW-rulesButtonUp.getWidth())/2 &&
						x < ((screenW-rulesButtonUp.getWidth())/2) + rulesButtonUp.getWidth() &&
						y > (int)(screenH*0.6+playButtonUp.getHeight()) &&
						y < (int)(screenH*0.6+playButtonUp.getHeight()) + rulesButtonUp.getHeight()) {
						rulesButtonPressed = true;
				}
				break;
				
			case MotionEvent.ACTION_MOVE:
				
				break;
			case MotionEvent.ACTION_UP:
	        	if (playButtonPressed) {
		        	Intent gameIntent = new Intent(myContext, CribbageActivity.class);
		        	gameIntent.putExtra("soundbool",soundOn);
		        	myContext.startActivity(gameIntent);	        		
	        	}
				else if (rulesButtonPressed) {
					Intent rulesIntent = new Intent(myContext, RulesActivity.class);

					myContext.startActivity(rulesIntent);
				}
				playButtonPressed = false;
				rulesButtonPressed = false;
				break;
		}
		
		invalidate();
		return true;
	}
}
