package ko.Cribbage.card;

import java.util.ArrayList;

public class Hand {
    //arraylist containing the cards in hand
    public ArrayList<Card> hand = new ArrayList<Card>();
    //each index corresponds to hand's index and indicates the turn it was played
    public int[] played;
    //array containing number of each rank in hand
    private int[] numberof;

    /**
     * Constructor
     */
    public Hand() {
        //initalize to 16 because straight checks next 5 indexes  max card is jack or 11, so 16 should prevent null pointers
        numberof=new int[16];
        played = new int[4];
    }

    /**
     * clears arraylist and all member variables
     */
    public void newHand(){
        hand.clear();
        played=new int[4];
        numberof=new int[16];
    }

    /**
     * takes in a card adds it to the arraylist
     * then calls all of the separate scoring functions.
     * @param discard card that was discarded at end of play
     * @return score of hand
     */
    public int scoreHand(Card discard){
        if(hand.size()!=4) {
            return -1;
        }
        int score =0;
        hand.add(discard);
        score+=jackScore();
        score+=pairScore();
        score+=flushScore();
        score+=straightScore();
        score+=fifteenScore();
        //remove discard card from hand
        //because we add the cards back into the deck
        hand.remove(4);
        return score;
    }

    /**
     * played cards are not removed from the hand, so this checks the played array
     * @return true if all cards have been played
     */
    public boolean isEmpty() {
        for(int cardplayed : played) {
            if (cardplayed == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * adds turn the card was played to its index
     * @param index of card played
     * @param turn card was played
     */
    public void play(int index,int turn) {
        played[index]=turn;
    }

    /**
     * discard function
     * @param index of card to discard
     * @return card that will be added to crib
     */
    public Card discard (int index) {
        Card tocrib=hand.get(index);
        hand.remove(index);
        return tocrib;
    }

    /**
     * finds pairs and scores them
     * counts number of each rank and then uses later in straight scoring
     * numberof implementation taken from boulter.com/cribbage/ and changed around
     * @return points from pairs
     */
    private int pairScore(){
        for (int i = 0; i <5; i++){
            //0 indexed so we just subtract one
            numberof[hand.get(i).getRank()-1]++;
        }
        int score=0;
        for (int i = 0; i < 13; i++){
            //0 pts, 2 pts ,6 pts , and 12 pts
            score += (numberof[i]-1)*numberof[i];
        }
        return score;
    }

    private int jackScore(){
        int score=0;
        //original hand
        for (int i = 0; i < 4; i++){
            if(hand.get(i).getRank()==11 && hand.get(i).getSuit()== hand.get(4).getSuit()) {
                score++;
            }
        }
        return score;
    }

    /**
     * calculates flush score
     * checks first 4 cards and then discard card
     * @return points from flushes
     */
    private int flushScore(){
        //checks suit of first four cards to be same
        if (hand.get(0).getSuit() == hand.get(1).getSuit() && hand.get(1).getSuit() == hand.get(2).getSuit() &&
                hand.get(2).getSuit() == hand.get(3).getSuit()){
            //check discard card suit
            if (hand.get(0).getSuit() == hand.get(4).getSuit()){
                return 5;
            }
            else{
                return 4;
            }
        }
        return 0;
    }

    /**
     * straight scoring reuses the numberof array from pair scoring
     * also taken from boulter.com/cribbage/ with one correction made
     * @return points from straights
     */
    private int straightScore(){
        int score=0;
        //highest min card in straight is jack so stop at 11
        for (int i = 0; i < 11; i++)
        {
            if (numberof[i] > 0 && numberof[i + 1] > 0 && numberof[i + 2] > 0){
                //run of 3 exists
                if (numberof[i + 3] > 0){
                    // run of 4 exists
                    if (numberof[i + 4] > 0){
                        // run of 5
                        numberof[i + 4] = 0;
                        score += 5;
                    }
                    else{
                        int sum = numberof[i] + numberof[i + 1] + numberof[i + 2] + numberof[i + 3];
                        // if 4 then one run exists such as 1,2,3,4
                        if (sum == 4){
                            score += 4;
                        }
                        // if 5 then count twice because a unique run can be created from a duplicate card
                        // 1,2,3,4,4 two runs of 1,2,3,4
                        else if (sum == 5){
                            score += 8;
                        }
                    }
                    break;
                }
                else{
                    int sum = numberof[i] + numberof[i + 1] + numberof[i + 2];
                    boolean threedupes=false;
                    if(numberof[i]==3 || numberof[i + 1]==3 || numberof[i + 2]==3){
                        threedupes=true;
                    }
                    //one run of 3
                    if (sum == 3){
                        score += 3;
                    }
                    //the run of 3 has a duplicate
                    // 1,2,3,3 counts 1,2,3 twice
                    else if (sum == 4){
                        score += 6;
                    }
                    //two duplicates exist
                    // 1,2,3,3,3 counts 1,2,3 three times
                    else if (sum == 5 && threedupes){
                        score += 9;
                    }
                    // or 1,2,2,3,3 counts 1,2,3 four times
                    //boulter code did this wrong added the threedupe logic to differentiate these cases.
                    else{
                        score+=12;
                    }
                }
                break;
            }
        }
        return score;
    }

    /**
     * fifteen scoring takes each combination of 2,3,4,5 cards and adds 2 for all that sum to 15
     * @return points from fifteens
     */
    private int fifteenScore(){
        int score=0;
        //2 card fifteens 5C2 = 10 combinations
        // 4,3,2,1= 10
        for(int i=0;i<hand.size()-1;i++){
            for(int j=i+1;j<hand.size();j++){
                if(hand.get(i).getScoreValue()+hand.get(j).getScoreValue()==15){
                    score+=2;
                }
            }
        }
        //3 card 5C3 = 10 combinations
        //  i j k combinations
        //  0-1-2,3,4 0-2-3,4 0-3-4 1-2-3,4 1-3-4 2-3-4
        //  3         2       1     2       1     1
        for(int i=0;i<hand.size()-2;i++){
            for(int j=i+1;j<hand.size()-1;j++){
                for(int k=j+1;k<hand.size();k++)
                if(hand.get(i).getScoreValue()+hand.get(j).getScoreValue()+hand.get(k).getScoreValue()==15){
                    score+=2;
                }
            }
        }
        //4 card
        //5C4 is 5 so just iterate through all 5 combinations
        //0-3 %5 is cards 0,1,2,3.
        //hand size is 5 at this point so 4,5,6,7%5 gives cards 4,0,1,2
        for(int i=0; i<hand.size();i++){
            if(hand.get((i)%5).getScoreValue()+hand.get((i+1)%5).getScoreValue()+hand.get((i+2)%5).getScoreValue()+hand.get((i+3)%5).getScoreValue()==15){
                score+=2;
            }
        }
        //5 card
        //1 combination just sum all
        if(hand.get(0).getScoreValue()+hand.get(1).getScoreValue()+hand.get(2).getScoreValue()+hand.get(3).getScoreValue()+hand.get(4).getScoreValue()==15){
            score+=2;
        }


        return score;
    }

}



