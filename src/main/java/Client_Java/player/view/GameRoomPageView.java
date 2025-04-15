package Client_Java.player.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.util.List;
import java.util.Map;

public class GameRoomPageView {
    @FXML
    private Label blank0;
    @FXML
    private Label blank1;
    @FXML
    private Label blank2;
    @FXML
    private Label blank3;
    @FXML
    private Label blank4;
    @FXML
    private Label blank5;
    @FXML
    private Label blank6;
    @FXML
    private Label blank7;
    @FXML
    private Label blank8;
    @FXML
    private Label blank9;
    @FXML
    private Label blank10;
    @FXML
    private Label blank11;
    @FXML
    private Label blank12;
    @FXML
    private Label blank13;
    @FXML
    private Label blank14;
    @FXML
    private Label blank15;
    @FXML
    private Label blank16;
    @FXML
    private Label blank17;

    @FXML
    private ImageView catBottomHead;
    @FXML
    private ImageView catEyes;
    @FXML
    private ImageView catLeftWhiskers;
    @FXML
    private ImageView catRightWhiskers;
    @FXML
    private ImageView catShadow;
    @FXML
    private ImageView catSnout;
    @FXML
    private ImageView catTopHead;

    @FXML
    private ImageView currentPlayerR1Stdg;
    @FXML
    private ImageView currentPlayerR2Stdg;
    @FXML
    private ImageView currentPlayerR3Stdg;

    @FXML
    private Label lifeCountLabel;

    @FXML
    private ImageView oppPlayerR1Stdg;
    @FXML
    private ImageView oppPlayerR2Stdg;
    @FXML
    private ImageView oppPlayerR3Stdg;

    // Getter methods for UI elements
    public Label getBlank0() { return blank0; }
    public Label getBlank1() { return blank1; }
    public Label getBlank2() { return blank2; }
    public Label getBlank3() { return blank3; }
    public Label getBlank4() { return blank4; }
    public Label getBlank5() { return blank5; }
    public Label getBlank6() { return blank6; }
    public Label getBlank7() { return blank7; }
    public Label getBlank8() { return blank8; }
    public Label getBlank9() { return blank9; }
    public Label getBlank10() { return blank10; }
    public Label getBlank11() { return blank11; }
    public Label getBlank12() { return blank12; }
    public Label getBlank13() { return blank13; }
    public Label getBlank14() { return blank14; }
    public Label getBlank15() { return blank15; }
    public Label getBlank16() { return blank16; }
    public Label getBlank17() { return blank17; }

    public ImageView getCatBottomHead() { return catBottomHead; }
    public ImageView getCatEyes() { return catEyes; }
    public ImageView getCatLeftWhiskers() { return catLeftWhiskers; }
    public ImageView getCatRightWhiskers() { return catRightWhiskers; }
    public ImageView getCatShadow() { return catShadow; }
    public ImageView getCatSnout() { return catSnout; }
    public ImageView getCatTopHead() { return catTopHead; }

    public ImageView getCurrentPlayerR1Stdg() { return currentPlayerR1Stdg; }
    public ImageView getCurrentPlayerR2Stdg() { return currentPlayerR2Stdg; }
    public ImageView getCurrentPlayerR3Stdg() { return currentPlayerR3Stdg; }

    public Label getLifeCountLabel() { return lifeCountLabel; }

    public ImageView getOppPlayerR1Stdg() { return oppPlayerR1Stdg; }
    public ImageView getOppPlayerR2Stdg() { return oppPlayerR2Stdg; }
    public ImageView getOppPlayerR3Stdg() { return oppPlayerR3Stdg; }

    // Method to update the blanks with the correct letters
    public void updateBlanks(String word, Map<Character, List<Integer>> letterPositions) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (letterPositions.containsKey(c)) {
                // Update the corresponding blank with the letter
                switch (i) {
                    case 0: blank0.setText(String.valueOf(c)); break;
                    case 1: blank1.setText(String.valueOf(c)); break;
                    case 2: blank2.setText(String.valueOf(c)); break;
                    case 3: blank3.setText(String.valueOf(c)); break;
                    case 4: blank4.setText(String.valueOf(c)); break;
                    case 5: blank5.setText(String.valueOf(c)); break;
                    case 6: blank6.setText(String.valueOf(c)); break;
                    case 7: blank7.setText(String.valueOf(c)); break;
                    case 8: blank8.setText(String.valueOf(c)); break;
                    case 9: blank9.setText(String.valueOf(c)); break;
                    case 10: blank10.setText(String.valueOf(c)); break;
                    case 11: blank11.setText(String.valueOf(c)); break;
                    case 12: blank12.setText(String.valueOf(c)); break;
                    case 13: blank13.setText(String.valueOf(c)); break;
                    case 14: blank14.setText(String.valueOf(c)); break;
                    case 15: blank15.setText(String.valueOf(c)); break;
                    case 16: blank16.setText(String.valueOf(c)); break;
                    case 17: blank17.setText(String.valueOf(c)); break;
                }
            }
        }
    }

    public boolean isBlankEmpty(int index) {
        switch (index) {
            case 0: return blank0.getText().isEmpty();
            case 2: return blank2.getText().isEmpty();
            case 3: return blank3.getText().isEmpty();
            case 4: return blank4.getText().isEmpty();
            case 5: return blank5.getText().isEmpty();
            case 6: return blank6.getText().isEmpty();
            case 7: return blank7.getText().isEmpty();
            case 8: return blank8.getText().isEmpty();
            case 9: return blank9.getText().isEmpty();
            case 10: return blank10.getText().isEmpty();
            case 11: return blank11.getText().isEmpty();
            case 12: return blank12.getText().isEmpty();
            case 13: return blank13.getText().isEmpty();
            case 14: return blank14.getText().isEmpty();
            case 15: return blank15.getText().isEmpty();
            case 16: return blank16.getText().isEmpty();
            case 17: return blank17.getText().isEmpty();
            default: throw new IllegalArgumentException("Invalid blank index");
        }
    }
}