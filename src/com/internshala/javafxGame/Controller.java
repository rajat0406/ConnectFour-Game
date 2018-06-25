package com.internshala.javafxGame;

import com.sun.scenario.effect.impl.sw.java.JSWBlend_BLUEPeer;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable{

	private static final int columns=7;
	private static final int rows=6;
	private static final int circle_diameter=80;
	private  static  final String player1disc_color="#24303E";
	private static final String player2disc_color="#4CAA88";

	private static String player1name="Player one";
	private static String player2name="Player two";

	private boolean isplayer1turn=true;
	private Disc[][] inserteddiscArray=new Disc[rows][columns];



	@FXML
	public GridPane rootGridpane;
	@FXML
	public Pane InserteddiscPane;
	@FXML
	public Label playernameLabel;

	@FXML
	public Button setButton;

	@FXML
	public TextField playerOneTextField,playerTwoTextField;

	private boolean isAllowedtoInsert=true;

	public void createPlayground() {
		setButton.setOnAction(event -> {
			/*playerOneTextField.setText("rajat rastogi");
			playerTwoTextField.setText("arsh justa");*/

		});
		Shape rectanglewithHoles=createclickable();

	    rootGridpane.add(rectanglewithHoles,0,1);
	    List<Rectangle> rectangleList=clickablecolumns();
		for (Rectangle rectangle:rectangleList) {
			rootGridpane.add(rectangle,0,1);
		}

	}
	private Shape createclickable()
	{
		Shape rectanglewithHoles = new Rectangle((columns+1) * circle_diameter,( rows+1) * circle_diameter);

		for (int r = 0; r < rows; r++)
		{
			for (int c=0;c<columns;c++)
			{
				Circle circle = new Circle();
				circle.setRadius(circle_diameter/2);
				circle.setCenterX(circle_diameter/2);
				circle.setCenterY(circle_diameter/2);

				circle.setSmooth(true);

				circle.setTranslateX(c*(circle_diameter+5)+(circle_diameter/4));
				circle.setTranslateY(r*(circle_diameter+5)+(circle_diameter/4));
				rectanglewithHoles =Shape.subtract(rectanglewithHoles,circle);
			}
		}
		rectanglewithHoles.setFill(Color.WHITE);
		return rectanglewithHoles;
	}
	private List<Rectangle> clickablecolumns()
	{
		List<Rectangle> rectangleList=new ArrayList<>();
		for (int c=0;c<columns;c++) {
			Rectangle rectangle = new Rectangle(circle_diameter, (rows + 1) * circle_diameter);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(c*(circle_diameter+5)+circle_diameter / 4);
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column=c;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedtoInsert) {
					isAllowedtoInsert = false;

					insertDisc(new Disc(isplayer1turn), column);
				}
			});
			rectangleList.add(rectangle);
		}
		return rectangleList;
	}
	private  void insertDisc(Disc disc,int c)
	{
		int row=rows-1;
		while(row>=0)
		{
			if(getDiscIfPresent(row,c)==null)
				break;
			row--;
		}
		if(row<0)
			return;

		inserteddiscArray[row][c]=disc;
		InserteddiscPane.getChildren().add(disc);
		disc.setTranslateX(c*(circle_diameter+5)+circle_diameter / 4);
		int currentrow=row;
		TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row *(circle_diameter+5)+circle_diameter / 4);
		translateTransition.setOnFinished(event -> {
			isAllowedtoInsert=true;

			if(gameEnded(currentrow,c))
			{
				gameOver();
				return;
			}

			isplayer1turn=!isplayer1turn;
			playernameLabel.setText(isplayer1turn? player1name:player2name);
		});
		translateTransition.play();

	}
	private boolean gameEnded(int row,int column)
	{
		List<Point2D> verticalPoint= IntStream.rangeClosed(row-3,row +3)
				.mapToObj(r -> new Point2D(r,column)).collect(Collectors.toList());

		List<Point2D> horizontalPoint= IntStream.rangeClosed(column-3,column +3)
				.mapToObj(col -> new Point2D(row,col)).collect(Collectors.toList());
		Point2D startpoint1 = new Point2D(row-3,column + 3);
		List<Point2D> diagonal1point = IntStream.rangeClosed(0,6).mapToObj(i ->startpoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startpoint2 = new Point2D(row-3,column-3);
		List<Point2D> diagonal2point = IntStream.rangeClosed(0,6).mapToObj(i ->startpoint2.add(i,i ))
				.collect(Collectors.toList());


		
		boolean isended=checkCombination(verticalPoint) || checkCombination(horizontalPoint)
				|| checkCombination(diagonal1point) || checkCombination(diagonal2point);

		return  isended;

	}

	private boolean checkCombination(List<Point2D> Points) {
		int chain=0;
		for (Point2D point: Points ){

			int rowIndexForArray= (int) point.getX();
			int columnIndexForArray= (int) point.getY();
			Disc disc=getDiscIfPresent(rowIndexForArray,columnIndexForArray);
			if(disc !=null && disc.isPlayerOneMove==isplayer1turn)
			{
				chain++;
				if(chain==4)
					return true;
			}else
			{
				chain=0;
			}
			
		}
		return false;
	}
	private Disc getDiscIfPresent(int row,int column)
	{
		if(row >= rows || row <0 || column>=columns || column<0)
		{
			return null;
		}
		return inserteddiscArray[row][column];
	}

	private void gameOver()
	{
		String winner=isplayer1turn ? player1name:player2name;
		System.out.println("The Winner is " + winner);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is " + winner);
		alert.setContentText("Want to Play again ?");

		ButtonType ybt=new ButtonType("Yes");
		ButtonType nbt=new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(ybt,nbt);

		Platform.runLater(()->{
			Optional<ButtonType> btnClicked =alert.showAndWait();
			if(btnClicked.isPresent()  &&  btnClicked.get()==ybt)
			{
				resetGame();

			}
			else
			{
				Platform.exit();
				System.exit(0);
			}

		});
	}

	public void resetGame() {
		InserteddiscPane.getChildren().clear();
		for (int row = 0; row < inserteddiscArray.length; row++) {
			for (int col=0; col < inserteddiscArray[row].length;col++)
			{
				inserteddiscArray[row][col]=null;
			}
		}
		isplayer1turn=true;
		playernameLabel.setText(player1name);

		createPlayground();
	}

	private static class Disc extends Circle{
		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove)
		{
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius(circle_diameter/2);
			setFill(isPlayerOneMove?Color.valueOf(player1disc_color):Color.valueOf(player2disc_color));
			setCenterX(circle_diameter/2);
			setCenterY(circle_diameter/2);

		}
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {



	}
}
