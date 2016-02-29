package ko.Cribbage.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ko.Cribbage.R;
import ko.Cribbage.card.Card;
import ko.Cribbage.card.Hand;

/**
 * Cribbage game
 * @author Keshine O'Young
 *
 */
public class CribbageView extends View {
	
	private Paint highlightPaint;
	private Paint textPaint;

	private Context myContext;
	// drawing card variables
	private int screenW;
	private int screenH;
	private int scaledCardW;
	private int scaledCardH;
	private int cardspacing;
	private float scale;
	private Bitmap cardBack;
	// card movement
	// private final int SPEED = 20;
	private int movingCardIdx = -1;
	private int movingCardIdcomp = -1;
	private int movingX;
	private int movingY;
	private int computerX;
	private int computerY;
	private int destx;
	private int desty; // initialize in initCards because doesn't change


	private boolean[] selectedCards=new boolean[6];
	private int discardready;
	// sounds from soundbible.com
	private static SoundPool sounds;
	private int playSound;
	private int youwinSound;
	private int scoringSound;
	private int youloseSound;
	private int compplaySound;
	public boolean soundOn = true;

	// hands and cards
	private List<Card> deck = new ArrayList<Card>();
	private Hand myHand = new Hand();
	private Hand oppHand = new Hand();
	private Hand cribHand = new Hand();
	private Hand runningHand = new Hand();
	private Card discard;
	// score
	private int myScore = 0;
	private int oppScore = 0;
	// score/turn/round tracking
	private boolean myTurn;
	private boolean myDeal;
	private int turn;
	private int runningtotal;
	private boolean show = false;
	private boolean showcrib = false;
	private boolean shownext = false;



	public CribbageView(Context context) {
		super(context);
		myContext = context;
		scale = myContext.getResources().getDisplayMetrics().density;
		// text colors and attributes
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.WHITE);
		textPaint.setStyle(Paint.Style.STROKE);
		textPaint.setTextAlign(Paint.Align.LEFT);
		textPaint.setTextSize(scale * 25);
		textPaint.setShadowLayer(1, 3, 3,0xFF000000);
		// highlighted cards color
		highlightPaint = new Paint();
		highlightPaint.setAntiAlias(true);
		highlightPaint.setColor(Color.BLUE);
		ColorFilter filter = new LightingColorFilter(Color.BLUE, 1);
		highlightPaint.setColorFilter(filter);
		// all of the sounds used
		sounds = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
		playSound = sounds.load(myContext, ko.Cribbage.R.raw.blip2,1);
		compplaySound = sounds.load(myContext, ko.Cribbage.R.raw.compmove,1);
		youwinSound = sounds.load(myContext, ko.Cribbage.R.raw.youwin,1);
		youloseSound = sounds.load(myContext, ko.Cribbage.R.raw.youlose,1);
		scoringSound = sounds.load(myContext, ko.Cribbage.R.raw.chaching,1);
	}

	protected void onDraw(Canvas canvas) {
		// drawing scores in middle because knowing score at any point is important
		canvas.drawText("Opponent Score: " + Integer.toString(oppScore), 0, screenH / 2 - scaledCardH / 2 - 50, textPaint);
		canvas.drawText("My Score: " + Integer.toString(myScore), 0, screenH / 2 + scaledCardH / 2 + textPaint.getTextSize() + 50, textPaint);
		//this draws the cards in the crib during scoring
		if(showcrib) {
			float x;
			if (myDeal) {
				x = screenH - scaledCardH*2 - highlightPaint.getTextSize()*2 - (50 * scale);
			} else {
				x = highlightPaint.getTextSize()*2+(50*scale) + scaledCardH;
			}
			for(int i=0;i<4;i++) {
				canvas.drawBitmap(cribHand.hand.get(i).getBitmap(),
						i * (scaledCardW + cardspacing) + cardspacing / 2,
						x,
						null);
			}

		}
		// draw the crib face down
		// moves depending on who owns it
		else if(myHand.hand.size()==4) {
			float x;
			if (myDeal) {
				x = screenH - scaledCardH - highlightPaint.getTextSize() - (50 * scale);
			} else {
				x = highlightPaint.getTextSize() + (50 * scale);
			}
			for (int i = 0; i < 4; i++) {
				canvas.drawBitmap(cardBack,
						screenW - cardBack.getWidth() - 60 + i * (scale * 5), x,
						null);
			}
		}
		// computer card playing animation
		if(movingCardIdcomp != -1) {
			double dest = Math.sqrt(destx*destx+desty*desty);
			int dx = destx-computerX;
			int dy = desty-computerY;
			computerX+=30*dx/dest;
			computerY+=30*dy/dest;
			canvas.drawBitmap(oppHand.hand.get(movingCardIdcomp).getBitmap(),
					computerX,
					computerY,
					null);
			if(Math.abs(dx)<35 && Math.abs(dy)<35) {
				movingCardIdcomp = -1;
			}
		}
		// draws the cards after play has finished
		if(show && movingCardIdcomp==-1) {
			// draw the discard card
			canvas.drawBitmap(discard.getBitmap(),
					screenW-scaledCardW-cardspacing,
					screenH/2,
					null);
			// redraw both hands in front of each player
			for(int i=0;i<4;i++) {
				canvas.drawBitmap(myHand.hand.get(i).getBitmap(),
						i * (scaledCardW + cardspacing) + cardspacing / 2,
						screenH - scaledCardH - highlightPaint.getTextSize() - (50 * scale),
						null);
				canvas.drawBitmap(oppHand.hand.get(i).getBitmap(),
						i * (scaledCardW + cardspacing) + cardspacing / 2,
						highlightPaint.getTextSize() + (50 * scale),
						null);
			}
		}
		// draws cards during play
		else {
			// draw played cards in running total
			if (myHand.hand.size() == 4 && oppHand.hand.size() == 4) {
				// running total in mid right
				canvas.drawText("Total:", screenW - 200, screenH / 2, textPaint);
				canvas.drawText(Integer.toString(runningtotal), screenW - 200, screenH / 2 + textPaint.getTextSize(), textPaint);
				for (int i = 1; i < 15; i++) {
					for (int j = 0; j < myHand.hand.size(); j++) {
						if (myHand.played[j] == i) {
							canvas.drawBitmap(myHand.hand.get(j).getBitmap(),
									myHand.played[j] * scaledCardW / 3,
									(screenH / 2) - (scaledCardH / 2) + scaledCardW / 4,
									null);
							break;
						}
						if (oppHand.played[j] == i&& movingCardIdcomp != j) {
							canvas.drawBitmap(oppHand.hand.get(j).getBitmap(),
									oppHand.played[j] * scaledCardW / 3,
									desty,
									null);
							break;
						}
					}
				}
				// taken from c8's animated
				// draws hand and draws moving card
				for (int i = 0; i < myHand.hand.size(); i++) {
					if (i == movingCardIdx) {
						canvas.drawBitmap(myHand.hand.get(i).getBitmap(),
								movingX,
								movingY,
								null);
					} else {
						if (myHand.played[i] == 0) {
							canvas.drawBitmap(myHand.hand.get(i).getBitmap(),
									i * (scaledCardW + cardspacing) + cardspacing / 2,
									screenH - scaledCardH - highlightPaint.getTextSize() - (50 * scale),
									null);
						}
					}
					if (oppHand.played[i] == 0 && i!= movingCardIdcomp) {
						canvas.drawBitmap(cardBack,
								i * (scaledCardW + cardspacing) + cardspacing / 2,
								highlightPaint.getTextSize() + (50 * scale),
								null);
					}
				}
			}
			// draws hand during discard phase
			else {
				for (int i = 0; i < myHand.hand.size(); i++) {
					// selected cards are drawn using the highlightpaint, otherwise null.
					if (selectedCards[i]) {
						canvas.drawBitmap(myHand.hand.get(i).getBitmap(),
								i * (scaledCardW + cardspacing) + cardspacing / 2,
								screenH - scaledCardH - highlightPaint.getTextSize() - (50 * scale),
								highlightPaint);
					} else {
						canvas.drawBitmap(myHand.hand.get(i).getBitmap(),
								i * (scaledCardW + cardspacing) + cardspacing / 2,
								screenH - scaledCardH - highlightPaint.getTextSize() - (50 * scale),
								null);
					}
				}
				for (int i = 0; i < oppHand.hand.size(); i++) {
					canvas.drawBitmap(cardBack,
							i * (scaledCardW + cardspacing) + cardspacing / 2,
							highlightPaint.getTextSize() + (50 * scale),
							null);
				}
			}
		}

		invalidate();
	}

	@Override
	public void onSizeChanged (int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		screenW = w;
		screenH = h;
		Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), ko.Cribbage.R.drawable.card_back);
		// cribbage starting hand is 6, so I initialized width to 1/7 and gave each card some space.
		scaledCardW = (screenW/7);
		scaledCardH = (int) (scaledCardW*1.28);
		// space between each card
		cardspacing=(screenW-6*scaledCardW)/6;
		desty = (screenH / 2) - (scaledCardH / 2) - scaledCardW / 4;
		cardBack = Bitmap.createScaledBitmap(tempBitmap, scaledCardW, scaledCardH, false);
		initCards();
		dealCards();

		myDeal = new Random().nextBoolean();
		if(myDeal) {
			myTurn = false;
			Toast.makeText(myContext, "Your crib", Toast.LENGTH_SHORT).show();
		}
		else{
			myTurn=true;
			Toast.makeText(myContext, "Opponent's crib", Toast.LENGTH_SHORT).show();
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();
		int X = (int)event.getX();
		int Y = (int)event.getY();

		switch (eventaction) {

			case MotionEvent.ACTION_DOWN:
				// just in case stop accepting any input while computer is moving cards
				if(movingCardIdcomp!=-1) {
					break;
				}
				// This if else block is meant to pause the game during the scoring.
				// So that the player can see all the hands and the points obtained.
				// Each tap of screen progresses the scoring and then starts new hand at the end.
				if(show && !shownext) {
					if(soundOn) {
						AudioManager audiomanager = (AudioManager) this.myContext.getSystemService(Context.AUDIO_SERVICE);
						float volume = (float) audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
						sounds.play(scoringSound, volume, volume, 1, 0, 1);
					}
					scoredealerhand();
				}
				else if(shownext && !showcrib) {
					if(soundOn) {
						AudioManager audiomanager = (AudioManager) this.myContext.getSystemService(Context.AUDIO_SERVICE);
						float volume = (float) audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
						sounds.play(scoringSound, volume, volume, 1, 0, 1);
					}
					scorecribhand();
				}
				else if(showcrib) {
					initNewHand();
				}
				if(myHand.hand.size()==6) {
					for (int i = 0; i < myHand.hand.size(); i++) {
						if (X > (i * (scaledCardW + cardspacing) + cardspacing / 2)
								&& X < (i * (scaledCardW + cardspacing)) + (cardspacing / 2) + scaledCardW &&
								Y > screenH - scaledCardH - highlightPaint.getTextSize() - (50 * scale)) {
							if(!selectedCards[i]) {
								if(discardready<2) {
									selectedCards[i] = true;
									discardready++;
								}
							}
							else {
								selectedCards[i] = false;
								discardready--;
							}
						}
					}
					if(discardready==2) {
						discardtoCrib();
					}
				}
				else if (myTurn && myHand.hand.size()==4) {
					for (int i = 0; i < myHand.hand.size(); i++) {
						//skip over played cards
						if (myHand.played[i] != 0) {
							continue;
						}
						if (X > (i * (scaledCardW + cardspacing) + cardspacing / 2)
								&& X < (i * (scaledCardW + cardspacing)) + (cardspacing / 2) + scaledCardW &&
								Y > screenH - scaledCardH - highlightPaint.getTextSize() - (50 * scale)) {
							movingCardIdx = i;
							movingX = X - (int) (30 * scale);
							movingY = Y - (int) (70 * scale);
						}
					}
				}
				break;

			case MotionEvent.ACTION_MOVE:
				movingX = X-(int)(30*scale);
				movingY = Y-(int)(70*scale);
				break;

			case MotionEvent.ACTION_UP:
				if (movingCardIdx > -1 &&
						X > 0 &&
						X < (screenW) &&
						Y > (screenH/2)-(100*scale) &&
						Y < (screenH/2)+(100*scale)) {
					if ((myHand.hand.get(movingCardIdx).getScoreValue() + runningtotal) <= 31) {
						myHand.play(movingCardIdx, turn);
						turn++;
						runningtotal += myHand.hand.get(movingCardIdx).getScoreValue();
						runningHand.hand.add(myHand.hand.get(movingCardIdx));
						if(soundOn) {
							AudioManager audiomanager = (AudioManager) this.myContext.getSystemService(Context.AUDIO_SERVICE);
							float volume = (float) audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
							sounds.play(playSound, volume, volume, 1, 0, 1);
						}
						scorePlay(true);
						myTurn = false;
						ComputerAction();
					}
					else{
						// toast invalid play
						Toast.makeText(myContext, "Invalid play", Toast.LENGTH_SHORT).show();
					}
				}
				movingCardIdx = -1;
				break;
		}
		invalidate();
		return true;
	}

	/**
	 * c8's code for initializing all the cards with their images and creating the deck.
	 */
	private void initCards() {
		for (int i = 0; i < 4; i++) {
			for (int j = 101; j < 114; j++) {
				int tempId = j + (i*100);
				Card tempCard = new Card(tempId);

				// pkgName is the java class package name
				String pkgName = myContext.getPackageName();

				// 1) getResources() or myContext.getResources() doesn't matter
				// 2) nowhere is ".png" mentioned
				int resourceId = myContext.getResources().getIdentifier("card" + tempId, "drawable", pkgName);

				// decodeResource apparently interprets resourceId
				Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardW, scaledCardH, false);
				tempCard.setBitmap(scaledBitmap);
				deck.add(tempCard);
			}
		}
	}

	/**
	 * Add card to hand, remove from deck.
	 * @param handToDraw hand card will be added to
	 */
	private void drawCard(Hand handToDraw) {
		handToDraw.hand.add(deck.get(0));
		deck.remove(0);
	}

	/**
	 * deals 6 cards to each player
	 * sets turn to 1 so that the cards are not hugging the left wall.
	 */
	private void dealCards() {
		Collections.shuffle(deck, new Random());
		for (int i = 0; i < 6; i++) {
			drawCard(myHand);
			drawCard(oppHand);
		}
		turn = 1;
	}

	/**
	 * dialog box to confirm 2 selected cards
	 */
	private void discardtoCrib() {
		final Dialog confirmDialog = new Dialog(myContext);
		confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		confirmDialog.setContentView(R.layout.discard_dialog);
		Button ok_button = (Button) confirmDialog.findViewById(R.id.yesButton);
		ok_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// discard from back so index doesn't change
				for (int i = 5; i > -1; i--) {
					if (selectedCards[i]) {
						cribHand.hand.add(myHand.discard(i));
					}
					// reset selected for next hand
					selectedCards[i]=false;
				}
				// opponent just discards first two cards
				cribHand.hand.add(oppHand.discard(0));
				cribHand.hand.add(oppHand.discard(0));
				// reset counter for next hand
				discardready=0;
				if (!myTurn) {
					ComputerAction();
				}
				confirmDialog.dismiss();
			}
		});
		Button no_button = (Button) confirmDialog.findViewById(R.id.noButton);
		no_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				confirmDialog.dismiss();
			}
		});
		confirmDialog.show();
	}

	/**
	 * Handles all the play logic of who's turn it is.
	 * Checks when to end play or reset total
	 * Sends control back to player if it is their move.
	 * Calls computer action whenever it is computer's turn.
	 */
	private void ComputerAction() {
		// end hand if both players have emptied their hands
		if (myHand.isEmpty() && oppHand.isEmpty()) {
			// can't double count 31 points and last card points, so only add one if total is not 31
			if(runningtotal!=31)
				myScore+=1;
			// go and last card are essentially the same thing, but cribbage gives them different names
			Toast.makeText(myContext, "1 pt for last card", Toast.LENGTH_SHORT).show();
			scoreCheck();
			scorenondealerhand();
			return;
		}
		// if both players have no play, since play was just passed to computer
		// we score for human,reset total, and make computer play
		else if(!searchPlay(oppHand) && !searchPlay(myHand)) {
			if(runningtotal!=31) {
				myScore += 1;
				Toast.makeText(myContext, "1 pt for go", Toast.LENGTH_SHORT).show();
				scoreCheck();
			}
			resetTotal();
			compPlay();
		}
		// computer has a play and plays first valid card
		else if(searchPlay(oppHand)) {
			compPlay();
		}
		// if player has no plays after computer plays
		if(!searchPlay(myHand)) {
			// plays until computer has no plays
			do {
				if(movingCardIdcomp==-1) {
					compPlay();
				}
			}while(searchPlay(oppHand));
			// computer has last play in this instance
			// so scores to oppScore for these two
			if(myHand.isEmpty()&& oppHand.isEmpty()) {
				if(runningtotal!=31)
					oppScore+=1;
				Toast.makeText(myContext, "1 pt for last card", Toast.LENGTH_SHORT).show();
				scoreCheck();
				scorenondealerhand();
				return;
			}
			else {
				if(runningtotal!=31)
					oppScore+=1;
				Toast.makeText(myContext, "1 pt for go", Toast.LENGTH_SHORT).show();
				scoreCheck();
				resetTotal();
			}
		}
		myTurn=true;
	}

	/**
	 * Searches the input Hand for a valid play based on the cards in hand not played and the running total
	 */
	private boolean searchPlay(Hand thisHand) {
		for(int i=0;i<thisHand.hand.size();i++) {
			if(runningtotal+thisHand.hand.get(i).getScoreValue()<=31 && thisHand.played[i]==0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Action computer takes every time it is its turn and has valid play.
	 * sets movingcard to pause game for computer card animation
	 */
	private void compPlay() {
		for(int i=0;i<oppHand.hand.size();i++) {
			if(oppHand.played[i]==0 && oppHand.hand.get(i).getScoreValue()+runningtotal<=31) {
				movingCardIdcomp=i;
				computerX= i * (scaledCardW + cardspacing) + cardspacing / 2;
				computerY= (int) (highlightPaint.getTextSize() + (50 * scale));
				oppHand.play(movingCardIdcomp, turn);
				if(soundOn) {
					AudioManager audiomanager = (AudioManager) this.myContext.getSystemService(Context.AUDIO_SERVICE);
					float volume = (float) audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
					sounds.play(compplaySound, volume, volume, 1, 0, 1);
				}
				turn++;
				destx = oppHand.played[i] * scaledCardW / 3;
				runningtotal += oppHand.hand.get(i).getScoreValue();
				runningHand.hand.add(oppHand.hand.get(i));
				scorePlay(false);
				break;
			}
		}
	}

	/**
	 * Scores all moves made during the round play.
	 * checks if running total is 15 or 31
	 * Then checks for pairs and then a straight.
	 * @param player true for player, false for computer.
	 */
	private void scorePlay(boolean player) {
		int score=0;
		if(runningtotal==15) {
			score+=2;
			Toast.makeText(myContext, "2 pts for 15", Toast.LENGTH_SHORT).show();
		}
		if(runningtotal==31) {
			score+=2;
			Toast.makeText(myContext, "2 pts for 31", Toast.LENGTH_SHORT).show();
		}
		// pair checks
		int numpairs=0;
		// loop from last card played and check if card before it is same rank
		// when rank is not the same add to score and break out of loop
		for(int i=runningHand.hand.size()-1;i>0;i--) {
			if(runningHand.hand.get(i).getRank()==runningHand.hand.get(i-1).getRank()) {
				numpairs++;
			}
			else{
				break;
			}
		}
		if(numpairs==1) {
			score+=2;
			Toast.makeText(myContext, "2 pts for pair", Toast.LENGTH_SHORT).show();
		}
		else if(numpairs==2) {
			score+=6;
			Toast.makeText(myContext, "6 pts for pair royale", Toast.LENGTH_SHORT).show();
		}
		else if(numpairs==3) {
			score+=12;
			Toast.makeText(myContext, "12 pts for double pair royale", Toast.LENGTH_SHORT).show();
		}
		// straights
		// start at 2 because run must be at least 3 length
		// loops through played cards adding one card
		int straight=1;
		for(int i=2;i<runningHand.hand.size();i++) {
			int straightlen=1;
			// going to make array one longer and set last element to 100
			int[] sorthand = new int[i+2];
			// put rank into array from last played
			for(int j=0;j<sorthand.length-1;j++) {
				sorthand[j] = runningHand.hand.get(runningHand.hand.size()-1-j).getRank();
			}
			// set last element to 100 so that a straight will execute else statement
			// sort the array
			// check if sorted array is a straight
			// exits when run is broken, only assigns to score if the straight is the entire existing hand.
			sorthand[i+1]=100;
			Arrays.sort(sorthand);
			for(int k=0;k<sorthand.length;k++) {
				if(sorthand[k]+1==sorthand[k+1]) {
					straightlen++;
				}
				else{
					// sorthand length -1 because we added 100 to the array
					if(straightlen==sorthand.length-1) {
						if (straightlen > straight)
							straight = straightlen;
					}
					break;
				}
			}
		}
		if(straight>2) {
			score+=straight;
			Toast.makeText(myContext, straight + " pts for run", Toast.LENGTH_SHORT).show();
		}
		if(player) {
			myScore+=score;
		}
		else{
			oppScore+=score;
		}
		scoreCheck();
	}

	/**
	 * Order of scoring is important, so this function scores whoever is not dealer first
	 * Also sets a boolean flag show which is used in ontouch to pause scoring until motion down
	 */
	private void scorenondealerhand() {
		Collections.shuffle(deck, new Random());
		discard = deck.get(0);
		Toast.makeText(myContext, "Discard card is " + discard.toString(), Toast.LENGTH_SHORT).show();
		int x;
		if(myDeal) {
			x=oppHand.scoreHand(discard);
			oppScore+=x;
			Toast.makeText(myContext, "Opponent scored " + x + " pts", Toast.LENGTH_LONG).show();
		}
		else{
			x=myHand.scoreHand(discard);
			myScore+=x;
			Toast.makeText(myContext, "You scored " + x + " pts", Toast.LENGTH_LONG).show();
		}
		show=true;
		if(soundOn) {
			AudioManager audiomanager = (AudioManager) this.myContext.getSystemService(Context.AUDIO_SERVICE);
			float volume = (float) audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
			sounds.play(scoringSound, volume, volume, 1, 0, 1);
		}
		scoreCheck();
	}

	/**
	 * Continuation of the show scoring
	 * Is called in ontouch on a motion_down if the flag was set in scorenondealerhand().
	 * Implements a outlier scoring case where a jack is the discard card. point is given to dealer.
	 * Also sets shownext flag that sets up ontouchevent for scorecribhand()
	 */
	private void scoredealerhand() {
		int x=0;
		if(discard.getRank()==11) {
			x++;
		}
		if(myDeal) {
			x+=myHand.scoreHand(discard);
			myScore+=x;
			Toast.makeText(myContext, "You scored " + x + " pts", Toast.LENGTH_LONG).show();
		}
		else{
			x+=oppHand.scoreHand(discard);
			oppScore+=x;
			Toast.makeText(myContext, "Opponent scored " + x + " pts", Toast.LENGTH_LONG).show();
		}
		shownext=true;
	}

	/**
	 * End of the show scoring
	 * Scores the crib and gives the points to the dealer.
	 * Sets showcrib flag that sets ontouchevent to start new hand on motiondown.
	 */
	private void scorecribhand() {
		int x;
		if(myDeal) {
			x=cribHand.scoreHand(discard);
			myScore+=x;
			Toast.makeText(myContext, "Your crib scored " + x + " pts", Toast.LENGTH_LONG).show();
		}
		else{
			x=cribHand.scoreHand(discard);
			oppScore+=x;
			Toast.makeText(myContext, "Opponent's crib scored " + x + " pts", Toast.LENGTH_LONG).show();
		}
		showcrib=true;
		scoreCheck();
	}

	/**
	 * Checks if a player has won and calls endGame
	 */
	private void scoreCheck() {
		if(myScore>=121) {
			endGame(true);
		}
		else if(oppScore>=121) {
			endGame(false);
		}
	}

	/**
	 * resets all flags
	 * reads the cards from each hand to the deck and then empties each hand
	 * resets any running totals and then deals out new hands
	 */
	private void initNewHand() {
		show=false;
		showcrib=false;
		shownext=false;
		deck.addAll(myHand.hand);
		deck.addAll(oppHand.hand);
		deck.addAll(cribHand.hand);
		myHand.newHand();
		oppHand.newHand();
		cribHand.newHand();
		resetTotal();
		dealCards();
		if(myDeal) {
			myDeal=false;
			myTurn=true;
			Toast.makeText(myContext, "Opponent's crib", Toast.LENGTH_SHORT).show();
		}
		else{
			myDeal=true;
			myTurn=false;
			Toast.makeText(myContext, "Your crib", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Resets running total
	 * Increments the turn by 2 so that onDraw will show separation between each set of played cards
	 */
	private void resetTotal() {
		runningtotal=0;
		runningHand.newHand();
		turn+=2;
	}

	/**
	 * Ends the game, and gives dialog to start new game.
	 * @param player true if human won, false if comp won.
	 */
	private void endGame(boolean player) {
		final Dialog endGameDialog = new Dialog(myContext);
		endGameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		endGameDialog.setContentView(R.layout.end_game_dialog);
		TextView endGameText = (TextView) endGameDialog.findViewById(R.id.endGameText);
		if (player) {
			String msg = "You reached " + myScore + " points. You won! Would you like to play again?";
			endGameText.setText(msg);
			if(soundOn) {
				AudioManager audiomanager = (AudioManager) this.myContext.getSystemService(Context.AUDIO_SERVICE);
				float volume = (float) audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
				sounds.play(youwinSound, volume, volume, 1, 0, 1);
			}
		} else {
			String msg = "The computer reached " + oppScore + " points. Sorry, you lost. Would you like to play again?";
			endGameText.setText(msg);
			if(soundOn) {
				AudioManager audiomanager = (AudioManager) this.myContext.getSystemService(Context.AUDIO_SERVICE);
				float volume = (float) audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
				sounds.play(youloseSound, volume, volume, 1, 0, 1);
			}
		}
		Button newGameButton = (Button) endGameDialog.findViewById(R.id.newGameButton);
		newGameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				myScore = 0;
				oppScore = 0;
				myDeal = new Random().nextBoolean();
				initNewHand();
				endGameDialog.dismiss();
			}
		});
		endGameDialog.show();
	}
}
