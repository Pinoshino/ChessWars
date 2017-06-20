import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

public class ChessWars extends JFrame {
	public ChessWars() {
		// タイトルを設定
		setTitle("ChessWars");

		// サイズ変更をできなくする
		setResizable(false);

		Container contentPane = getContentPane();

		// 情報パネルを作成する
		InfoPanel infoPanel = new InfoPanel();
		contentPane.add(infoPanel, BorderLayout.NORTH);

		// メインパネルを作成してフレームに追加
		MainPanel mainPanel = new MainPanel(infoPanel);
		contentPane.add(mainPanel, BorderLayout.CENTER);

		// パネルサイズに合わせてフレームサイズを自動設定
		pack();
	}

	public static void main(String args[]) {
		ChessWars frame = new ChessWars();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
