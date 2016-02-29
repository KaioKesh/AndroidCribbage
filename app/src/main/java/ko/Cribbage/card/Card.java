package ko.Cribbage.card;

import android.graphics.Bitmap;

public class Card {

	private int id;
	private int suit;
	private int rank;
	protected Bitmap bmp;
	private int scoreValue;
	
	public Card(int newId) {
		id = newId;
		suit = Math.round((id/100) * 100);
		rank = id - suit;
		if (rank > 9 && rank < 14) {
			scoreValue = 10;
		} else {
			scoreValue = rank;
		}
	}

	public int getScoreValue() {
		return scoreValue;
	}
	
	public void setBitmap(Bitmap newBitmap) {
		bmp = newBitmap; 
	}
	
	public Bitmap getBitmap() {
		return bmp;
	}
	
	public int getId() {
		return id;
	}
	
	public int getSuit() {
		return suit;
	}
	
	public int getRank() {
		return rank;
	}

	@Override
	public String toString() {
		String strsuit="";
		if(suit==100){
			strsuit="Diamonds";
		}
		else if(suit==200){
			strsuit="Clubs";
		}
		else if(suit==300){
			strsuit="Hearts";
		}
		else if(suit==400){
			strsuit="Spades";
		}
		String strrank = "";
		if(rank > 1 && rank < 11) {
			strrank=rank+"";
		}
		else if(rank == 1){
			strrank="Ace";
		}
		else if(rank == 11){
			strrank="Jack";
		}
		else if(rank == 12){
			strrank="Queen";
		}
		else if(rank == 13){
			strrank="King";
		}

		return strrank + " of " + strsuit;
	}

}



