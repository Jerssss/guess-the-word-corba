package Client_Java.player.view;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.ImageView;

public class GameRoomView {
    @FXML private BorderPane rootPane;
    @FXML private Pane       gameContentPane;

    @FXML private Label      roundLabel;
    @FXML private Label      lifeCountLabel;
    @FXML private Label      timerLabel;           // ← new timer label

    @FXML private FlowPane   wordFlow;
    @FXML private FlowPane   alphabetFlow;

    @FXML private ImageView  catShadow;
    @FXML private ImageView  catTopHead;
    @FXML private ImageView  catEyes;
    @FXML private ImageView  catSnout;
    @FXML private ImageView  catLeftWhiskers;
    @FXML private ImageView  catRightWhiskers;
    @FXML private ImageView  catBottomHead;

    // getters for Controller
    public BorderPane getRootPane()        { return rootPane;       }
    public Pane       getGameContentPane() { return gameContentPane;}
    public Label      getRoundLabel()      { return roundLabel;     }
    public Label      getLifeCountLabel()  { return lifeCountLabel; }
    public Label      getTimerLabel()      { return timerLabel;     } // ← expose it

    public FlowPane   getWordFlow()        { return wordFlow;       }
    public FlowPane   getAlphabetFlow()    { return alphabetFlow;   }

    public ImageView getCatShadow()        { return catShadow;       }
    public ImageView getCatTopHead()       { return catTopHead;      }
    public ImageView getCatEyes()          { return catEyes;         }
    public ImageView getCatSnout()         { return catSnout;        }
    public ImageView getCatLeftWhiskers()  { return catLeftWhiskers; }
    public ImageView getCatRightWhiskers() { return catRightWhiskers;}
    public ImageView getCatBottomHead()    { return catBottomHead;   }
}
