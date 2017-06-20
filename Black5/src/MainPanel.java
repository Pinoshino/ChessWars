import java.awt.*;
import java.awt.event.*;
import java.awt.Container;
import java.applet.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.lang.*;
import javax.swing.*;

//import Client.MesgRecvThread;

public class MainPanel extends JPanel implements MouseListener {

	PrintWriter out; // 出力用のライター

	// マスの大きさ
	private static final int GS = 40;

	// マスの数
	private static final int MASU = 15;

	// 空白
	private int BLANK = -1;

	// パネルサイズ
	public static final int WIDTH = 600;
	public static final int HEIGHT = 600;

	// キャラクター画像
	public static Image[] charaImage = new Image[14];

	// (int x, int y, int ap , int hp, int charaNo, string name)
	private Chara[] piece = { new Chara(4, 13, 1, 10, 0, "rook_black"), new Chara(10, 13, 1, 10, 1, "bishop_black"),
			new Chara(6, 13, 2, 12, 2, "silver_black"), new Chara(8, 13, 3, 15, 3, "gold_black"),
			new Chara(5, 14, 5, 10, 4, "knight_black"), new Chara(9, 14, 2, 12, 5, "queen_black"),
			new Chara(7, 14, 1, 15, 6, "king_black"), new Chara(10, 1, 1, 10, 7, "rook_white"),
			new Chara(4, 1, 1, 10, 8, "bishop_white"), new Chara(8, 1, 2, 12, 9, "silver_white"),
			new Chara(6, 1, 3, 15, 10, "gold_white"), new Chara(9, 0, 5, 10, 11, "knight_white"),
			new Chara(5, 0, 2, 12, 12, "queen_white"), new Chara(7, 0, 1, 15, 13, "king_white"), };

	// 盤面
	private int[][] board = new int[MASU][MASU];

	// ゲーム状態
	/*
	 * 0 : スタート画面 1 : プレイ中（移動前） 2 : プレイ中（移動後） 3 : 勝った時 4 : 負けた時
	 */
	private int gameState = 0;

	// 選択した駒の番号
	private int tampleNo = -1;

	// フラグ管理
	private int myturn = 1;
	// private int enemyturn = 0;

	private int win;
	private int lose;

	// 情報パネルへの参照
	private InfoPanel infoPanel;

	public MainPanel(InfoPanel infoPanel) {

		// 名前を入れる画面
		String myName = JOptionPane.showInputDialog(null, "名前を入力してください", "名前の入力", JOptionPane.QUESTION_MESSAGE);
		if (myName.equals("")) {
			myName = "No name"; // 名前がないときは，"No name"とする
		}

		// パネルの推奨サイズを設定、pack()するときに必要
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.infoPanel = infoPanel;

		// サーバに接続する
		Socket socket = null;
		try {
			// "localhost"は，自分内部への接続．localhostを接続先のIP
			// Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
			// 5296はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
			socket = new Socket("localhost", 9000); // pnum
		} catch (UnknownHostException e) {
			System.err.println("ホストの IP アドレスが判定できません: " + e);
		} catch (IOException e) {
			System.err.println("エラーが発生しました: " + e);
		}

		MesgRecvThread mrt = new MesgRecvThread(socket, myName);// 受信用のスレッドを作成する
		mrt.start();// スレッドを動かす（Runが動く）

		// 変数などの初期化
		initBoard();

		// チェスの駒の画像をロード
		loadImage();

		// MouseListenerを登録
		addMouseListener(this);
	}

	// メッセージ受信のためのスレッド
	public class MesgRecvThread extends Thread {

		Socket socket;
		String myName;

		public MesgRecvThread(Socket s, String n) {
			socket = s;
			myName = n;
		}

		// 通信状況を監視し，受信データによって動作する
		public void run() {
			try {
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(myName);// 接続の最初に名前を送る。送るときはout.println(s);
				while (true) {
					String information = br.readLine();// データを一行分だけ読み込んでみる
					if (information != null) {// informationにデータが入っているとき
						System.out.println(information);// デバッグ（動作確認用）にコンソールに出力する
						String[] inputTokens = information.split(" "); // 入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];// コマンドの取り出し．１つ目の要素を取り出す
						if (cmd.equals("MOVE")) {// cmdの文字と"MOVE"が同じか調べる．同じ時にtrueとなる
							// MOVEの時の処理(コマの移動の処理)
							int gs = Integer.parseInt(inputTokens[1]);
							int tampleNo_enemy = Integer.parseInt(inputTokens[2]);

							if (gs != 0) {
								gameState = gs;
							}

							if (tampleNo_enemy != -1 && inputTokens[6].equals("USUAL")) {

								int new_x = Integer.parseInt(inputTokens[3]);// 数値に変換する
								int new_y = Integer.parseInt(inputTokens[4]);// 数値に変換する
								int nowturn = Integer.parseInt(inputTokens[5]);

								board[piece[tampleNo_enemy].getY()][piece[tampleNo_enemy].getX()] = BLANK;
								board[new_y][new_x] = tampleNo_enemy;
								piece[tampleNo_enemy].moveChara(new_x, new_y);
								myturn = nowturn;

							}

							else if (tampleNo_enemy != -1 && inputTokens[6].equals("ATTACK")) {
								int nowturn = Integer.parseInt(inputTokens[5]);
								int pre_x = Integer.parseInt(inputTokens[3]);// 数値に変換する
								int pre_y = Integer.parseInt(inputTokens[4]);
								int new_x = Integer.parseInt(inputTokens[7]);
								int new_y = Integer.parseInt(inputTokens[8]);
								if (tampleNo_enemy >= 7) {
									piece[board[new_y][new_x]].damage(piece[tampleNo_enemy].getAp());
								}
								if (piece[board[new_y][new_x]].getHp() <= 0) {
									System.out.println(new_x);
									System.out.println(pre_x);
									System.out.println(tampleNo_enemy);
									board[new_y][new_x] = tampleNo_enemy; // その駒を指定されたマスへ配置
									board[pre_y][pre_x] = BLANK; // 元にいた位置はBLANKにする
									piece[tampleNo_enemy].moveChara(new_x, new_y); // そのキャラの持っている座標を動かす
									gameState = 1;
									endGame();

								}

								myturn = nowturn;

							}

							repaint();

						}
					} else {
						break;
					}

				}
				socket.close();
			} catch (IOException e) {
				System.err.println("エラーが発生しました: " + e);
			}
		}
	}

	public void paintComponent(Graphics g) {
		// int i;
		super.paintComponent(g);

		// 盤面を描いたり、フィールドを描いたりする
		// 盤面の描画
		switch (gameState) {
		case 0:
			drawTextCentering(g, "ChessWars");
			break;

		case 1:
			// 盤面の描画
			drawBoard(g);
			// キャラの描画
			drawChara(g);
			if (tampleNo != -1)
				drawMove(g);
			break;

		case 2:
			drawBoard(g);
			drawChara(g);
			break;

		case 3:
			drawBoard(g);
			drawChara(g);
			drawTextCentering(g, "You win");
			break;

		case 4:
			drawBoard(g);
			drawChara(g);
			drawTextCentering(g, "You lose");
			break;

		}

	}

	public void mouseClicked(MouseEvent e) {

		int x = e.getX() / GS;
		int y = e.getY() / GS;

		switch (gameState) {
		case 0:
			gameState = 1;
			String msg1 = "MOVE" + " " + gameState + " " + tampleNo + " " + 3 + " " + 4 + " " + 5 + " " + "START";
			out.println(msg1);// 送信データをバッファに書き出す
			out.flush();
			repaint();

			break;

		case 1:
			// 駒には当たらず、空いているマスに移動するとき
			if (board[y][x] == BLANK && tampleNo <= 6 && tampleNo >= 0 && myturn == 1) {

				if (limit(x, y) && checkPiece(x, y)) {
					board[y][x] = tampleNo; // その駒を指定されたマスへ配置
					board[piece[tampleNo].getY()][piece[tampleNo].getX()] = BLANK; // 元にいた位置はBLANKにする
					piece[tampleNo].moveChara(x, y); // そのキャラの持っている座標を動かす
					myturn = 0;
					String msg = "MOVE" + " " + gameState + " " + tampleNo + " " + piece[tampleNo].getX() + " " + piece[tampleNo].getY() + " " + myturn + " " + "USUAL";

					// サーバに情報を送る
					out.println(msg);// 送信データをバッファに書き出す
					out.flush();// 送信データをフラッシュ（ネットワーク上にはき出す）する

					tampleNo = -1;

					// 再描画する
					repaint();
					JOptionPane.showMessageDialog(this, "Turn End", "Info", JOptionPane.PLAIN_MESSAGE, null);
				} else
					infoPanel.setCharaLabel("Noname", -1, -1);

				break;

			} else if (board[y][x] >= 7 && tampleNo <= 6 && tampleNo >= 0 && limit(x, y) && myturn == 1) {
				int option = JOptionPane.showConfirmDialog(this, "Attack？", "attack", JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE);
				if (option == 0) {
					piece[board[y][x]].damage(piece[tampleNo].getAp());
					if (piece[board[y][x]].getHp() <= 0) {
						board[y][x] = BLANK;
						repaint();
					}
					myturn = 0;
					JOptionPane.showMessageDialog(this, "Turn End", "Info", JOptionPane.PLAIN_MESSAGE, null);
				}

				String msg = "MOVE" + " " + gameState + " " + tampleNo + " " + piece[tampleNo].getX() + " " + piece[tampleNo].getY() + " " + myturn + " " + "ATTACK" + " " + x + " " + y;

				// サーバに情報を送る

				out.println(msg);// 送信データをバッファに書き出す
				out.flush();// 送信データをフラッシュ（ネットワーク上にはき出す）する

				tampleNo = -1;

			}else if (board[y][x] != BLANK) {

				infoPanel.setCharaLabel(piece[board[y][x]].getName(), piece[board[y][x]].getHp(),piece[board[y][x]].getAp());
				tampleNo = board[y][x];
				if(tampleNo != -1){
					String msg = "MOVE" + " " + gameState + " " + tampleNo + " " + piece[tampleNo].getX() + " " + piece[tampleNo].getY() + " " + myturn + " " + "USUAL";

					// サーバに情報を送る
					out.println(msg);// 送信データをバッファに書き出す
					out.flush();// 送信データをフラッシュ（ネットワーク上にはき出す）する
				}

				repaint();

				break;
			}

			endGame();

			if (win == 0) {
				gameState = 2;
			} else if (lose == 0) {
				gameState = 3;
			}

			break;

		case 2:
			gameState = 0;
			// 盤面初期化
			initBoard();
			break;

		case 3:
			gameState = 0;
			// 盤面初期化
			initBoard();
			break;

		default:
			break;

		}

	}


	private boolean limit(int x, int y) {

		int tampleX = piece[tampleNo].getX();
		int tampleY = piece[tampleNo].getY();

		switch (tampleNo) {
		case 0:
			if (x == tampleX || y == tampleY) {
				return true;
			} else
				return false;
			// break;

		case 1:
			if (Math.abs(x - tampleX) == Math.abs(y - tampleY)) {
				return true;
			} else
				return false;

			// break;

		case 2:
			if (Math.abs(x - tampleX) <= 1 && Math.abs(y - tampleY) <= 1
					&& Math.abs(x - tampleX) == Math.abs(y - tampleY)) {
				return true;
			} else if (y == tampleY - 1 && x == tampleX) {
				return true;
			} else
				return false;

			// break;

		case 3:
			if (Math.abs(x - tampleX) <= 1 && Math.abs(y - tampleY) <= 1) {
				if (y == tampleY + 1) {
					if (x == tampleX + 1) {
						return false;
					} else if (x == tampleX - 1) {
						return false;
					} else
						return true;
				} else
					return true;
			} else
				return false;

			// break;

		case 4:
			if (y == tampleY + 2 || y == tampleY - 2) {
				if (x == tampleX + 1 || x == tampleX - 1) {
					return true;
				} else
					return false;
			} else if (y == tampleY + 1 || y == tampleY - 1) {
				if (x == tampleX + 2 || x == tampleX - 2) {
					return true;
				} else
					return false;
			} else
				return false;

			// break;

		case 5:
			if (Math.abs(x - tampleX) == Math.abs(y - tampleY) || x == tampleX || y == tampleY) {
				return true;
			} else
				return false;

			// break;

		case 6:
			if (Math.abs(x - tampleX) <= 1 && Math.abs(y - tampleY) <= 1) {
				return true;
			} else
				return false;

			// break;

		case 7:
			if (x == tampleX || y == tampleY) {
				return true;
			} else
				return false;
			// break;

		case 8:
			if (Math.abs(x - tampleX) == Math.abs(y - tampleY)) {
				return true;
			} else
				return false;

			// break;

		case 9:
			if (Math.abs(x - tampleX) <= 1 && Math.abs(y - tampleY) <= 1
					&& Math.abs(x - tampleX) == Math.abs(y - tampleY)) {
				return true;
			} else if (y == tampleY - 1 && x == tampleX) {
				return true;
			} else
				return false;

			// break;

		case 10:
			if (Math.abs(x - tampleX) <= 1 && Math.abs(y - tampleY) <= 1) {
				if (y == tampleY + 1) {
					if (x == tampleX + 1) {
						return false;
					} else if (x == tampleX - 1) {
						return false;
					} else
						return true;
				} else
					return true;
			} else
				return false;

			// break;

		case 11:
			if (y == tampleY + 2 || y == tampleY - 2) {
				if (x == tampleX + 1 || x == tampleX - 1) {
					return true;
				} else
					return false;
			} else if (y == tampleY + 1 || y == tampleY - 1) {
				if (x == tampleX + 2 || x == tampleX - 2) {
					return true;
				} else
					return false;
			} else
				return false;

			// break;

		case 12:
			if (Math.abs(x - tampleX) == Math.abs(y - tampleY) || x == tampleX || y == tampleY) {
				return true;
			} else
				return false;

			// break;

		case 13:
			if (Math.abs(x - tampleX) <= 1 && Math.abs(y - tampleY) <= 1) {
				return true;
			} else
				return false;

			// break;

		default:
			return false;

		// break;

		}
	}

	private boolean checkPiece(int x, int y) {

		int check = 1;

		int tampleX = piece[tampleNo].getX();
		int tampleY = piece[tampleNo].getY();

		switch (tampleNo) {
		case 0:
			if (x == tampleX) {
				if (y < tampleY) {
					for (int i = -1;; i--) {
						if (y > tampleY + i)
							break;
						if (board[tampleY + i][tampleX] != -1)
							check = 0;
					}
				} else if (y > tampleY) {
					for (int i = 1;; i++) {
						if (y < tampleY + i)
							break;
						if (board[tampleY + i][tampleX] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (y == tampleY) {
				if (x < tampleX) {
					for (int i = -1;; i--) {
						if (x > tampleX + i)
							break;
						if (board[tampleY][tampleX + i] != -1)
							check = 0;
					}
				} else if (x > tampleX) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else
				return false;

			// break;

		case 1:
			if (x > tampleX) {
				if (y > tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY + i][tampleX + i] != -1)
							check = 0;
					}
				} else if (y < tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY - i][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (x < tampleX) {
				if (y > tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY + i][tampleX + i] != -1)
							check = 0;
					}
				} else if (y < tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY - i][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else
				return false;

			// break;

		case 5:
			if (x == tampleX) {
				if (y < tampleY) {
					for (int i = -1;; i--) {
						if (y > tampleY + i)
							break;
						if (board[tampleY + i][tampleX] != -1)
							check = 0;
					}
				} else if (y > tampleY) {
					for (int i = 1;; i++) {
						if (y < tampleY + i)
							break;
						if (board[tampleY + i][tampleX] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (y == tampleY) {
				if (x < tampleX) {
					for (int i = -1;; i--) {
						if (x > tampleX + i)
							break;
						if (board[tampleY][tampleX + i] != -1)
							check = 0;
					}
				} else if (x > tampleX) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (x > tampleX) {
				if (y > tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY + i][tampleX + i] != -1)
							check = 0;
					}
				} else if (y < tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY - i][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (x < tampleX) {
				if (y > tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY + i][tampleX + i] != -1)
							check = 0;
					}
				} else if (y < tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY - i][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else
				return false;

			// break;

		case 7:
			if (x == tampleX) {
				if (y < tampleY) {
					for (int i = -1;; i--) {
						if (y > tampleY + i)
							break;
						if (board[tampleY + i][tampleX] != -1)
							check = 0;
					}
				} else if (y > tampleY) {
					for (int i = 1;; i++) {
						if (y < tampleY + i)
							break;
						if (board[tampleY + i][tampleX] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (y == tampleY) {
				if (x < tampleX) {
					for (int i = -1;; i--) {
						if (x > tampleX + i)
							break;
						if (board[tampleY][tampleX + i] != -1)
							check = 0;
					}
				} else if (x > tampleX) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else
				return false;

			// break;

		case 8:
			if (x > tampleX) {
				if (y > tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY + i][tampleX + i] != -1)
							check = 0;
					}
				} else if (y < tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY - i][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (x < tampleX) {
				if (y > tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY + i][tampleX + i] != -1)
							check = 0;
					}
				} else if (y < tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY - i][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else
				return false;

			// break;

		case 12:
			if (x == tampleX) {
				if (y < tampleY) {
					for (int i = -1;; i--) {
						if (y > tampleY + i)
							break;
						if (board[tampleY + i][tampleX] != -1)
							check = 0;
					}
				} else if (y > tampleY) {
					for (int i = 1;; i++) {
						if (y < tampleY + i)
							break;
						if (board[tampleY + i][tampleX] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (y == tampleY) {
				if (x < tampleX) {
					for (int i = -1;; i--) {
						if (x > tampleX + i)
							break;
						if (board[tampleY][tampleX + i] != -1)
							check = 0;
					}
				} else if (x > tampleX) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (x > tampleX) {
				if (y > tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY + i][tampleX + i] != -1)
							check = 0;
					}
				} else if (y < tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY - i][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else if (x < tampleX) {
				if (y > tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY + i][tampleX + i] != -1)
							check = 0;
					}
				} else if (y < tampleY) {
					for (int i = 1;; i++) {
						if (x < tampleX + i)
							break;
						if (board[tampleY - i][tampleX + i] != -1)
							check = 0;
					}
				}
				if (check == 0)
					return false;
				else
					return true;
			} else
				return false;

			// break;

		default:
			return true;

		}
	}

	private void endGame() {
		win = 0;
		lose = 0;
		for (int y = 0; y < MASU; y++) {
			for (int x = 0; x < MASU; x++) {
				if (board[y][x] == 13)
					win = 1;
				else if (board[y][x] == 6)
					lose = 1;
			}
		}

		if (win == 0)
			gameState = 3;
		else if (lose == 0)
			gameState = 4;

	}

	// �Ֆʂ�������
	private void initBoard() {
		for (int y = 0; y < MASU; y++) {
			for (int x = 0; x < MASU; x++) {
				board[y][x] = BLANK;
			}
		}
		// 初期配置
		// それぞれも駒の位置を盤上では数字1つで判定する

		board[13][4] = 0;
		board[13][10] = 1;
		board[13][6] = 2;
		board[13][8] = 3;
		board[14][5] = 4;
		board[14][9] = 5;
		board[14][7] = 6;

		board[1][10] = 7;
		board[1][4] = 8;
		board[1][8] = 9;
		board[1][6] = 10;
		board[0][9] = 11;
		board[0][5] = 12;
		board[0][7] = 13;

		// 黒番から始める
		// flagForWhite = false;
	}

	// 盤面を描く
	public void drawBoard(Graphics g) {
		// マスを塗りつぶす
		g.setColor(new Color(231, 225, 143, 250));
		g.fillRect(0, 0, WIDTH, HEIGHT);

		g.setColor(Color.black);
		// 縦線を引く
		for (int i = 1; i < MASU; i++) {
			g.drawLine(i * GS, 0, i * GS, HEIGHT);
		}
		// 横線を引く
		for (int i = 1; i < MASU; i++) {
			g.drawLine(0, i * GS, WIDTH, i * GS);
		}
		// 外枠を引く
		g.drawRect(0, 0, WIDTH, HEIGHT);
	}

	public void drawMove(Graphics g) {

		int tampleX = piece[tampleNo].getX();
		int tampleY = piece[tampleNo].getY();

		// System.out.println(tampleX);
		// System.out.println(tampleY);

		switch (tampleNo) {

		case 0:
			for (int i = tampleX + 1; i <= 14; i++) {
				if (board[tampleY][i] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(i * GS, tampleY * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleX - 1; i >= 0; i--) {
				if (board[tampleY][i] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(i * GS, tampleY * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleY + 1; i <= 14; i++) {
				if (board[i][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, i * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleY - 1; i >= 0; i--) {
				if (board[i][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, i * GS, GS, GS);
				} else
					break;
			}

			break;

		case 1:
			for (int i = 1;; i++) {
				if (i <= 14 - tampleX && i <= 14 - tampleY) {
					if (board[tampleY + i][tampleX + i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX + i) * GS, (tampleY + i) * GS, GS, GS);
					} else
						break;
				} else
					break;

			}
			for (int i = 1;; i++) {
				if (i <= 14 - tampleX && i <= tampleY) {
					if (board[tampleY - i][tampleX + i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX + i) * GS, (tampleY - i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= tampleX && i <= 14 - tampleY) {
					if (board[tampleY + i][tampleX - i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX - i) * GS, (tampleY + i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= tampleX && i <= tampleY) {
					if (board[tampleY - i][tampleX - i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX - i) * GS, (tampleY - i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}

			break;

		case 2:

			if (tampleX + 1 <= 14 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			break;

		case 3:
			if (tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0) {
				if (board[tampleY][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, tampleY * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14) {
				if (board[tampleY][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, tampleY * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			break;

		case 4:
			if (tampleX + 1 <= 14 && tampleY - 2 >= 0) {
				if (board[tampleY - 2][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY - 2) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY - 2 >= 0) {
				if (board[tampleY - 2][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY - 2) * GS, GS, GS);
				}
			}

			if (tampleX + 2 <= 14 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX + 2] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 2) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 2 >= 0 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX - 2] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 2) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX + 2 <= 14 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX + 2] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 2) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX - 2 >= 0 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX - 2] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 2) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14 && tampleY + 2 <= 14) {
				if (board[tampleY + 2][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY + 2) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY + 2 <= 14) {
				if (board[tampleY + 2][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY + 2) * GS, GS, GS);
				}
			}

			break;

		case 5:
			for (int i = tampleX + 1; i <= 14; i++) {
				if (board[tampleY][i] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(i * GS, tampleY * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleX - 1; i >= 0; i--) {
				if (board[tampleY][i] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(i * GS, tampleY * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleY + 1; i <= 14; i++) {
				if (board[i][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, i * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleY - 1; i >= 0; i--) {
				if (board[i][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, i * GS, GS, GS);
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= 14 - tampleX && i <= 14 - tampleY) {
					if (board[tampleY + i][tampleX + i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX + i) * GS, (tampleY + i) * GS, GS, GS);
					} else
						break;
				} else
					break;

			}
			for (int i = 1;; i++) {
				if (i <= 14 - tampleX && i <= tampleY) {
					if (board[tampleY - i][tampleX + i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX + i) * GS, (tampleY - i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= tampleX && i <= 14 - tampleY) {
					if (board[tampleY + i][tampleX - i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX - i) * GS, (tampleY + i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= tampleX && i <= tampleY) {
					if (board[tampleY - i][tampleX - i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX - i) * GS, (tampleY - i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}

			break;

		case 6:
			if (tampleX + 1 <= 14 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0) {
				if (board[tampleY][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, tampleY * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14) {
				if (board[tampleY][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, tampleY * GS, GS, GS);
				}
			}

			break;

		case 7:
			for (int i = tampleX + 1; i <= 14; i++) {
				if (board[tampleY][i] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(i * GS, tampleY * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleX - 1; i >= 0; i--) {
				if (board[tampleY][i] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(i * GS, tampleY * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleY + 1; i <= 14; i++) {
				if (board[i][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, i * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleY - 1; i >= 0; i--) {
				if (board[i][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, i * GS, GS, GS);
				} else
					break;
			}

			break;

		case 8:
			for (int i = 1;; i++) {
				if (i <= 14 - tampleX && i <= 14 - tampleY) {
					if (board[tampleY + i][tampleX + i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX + i) * GS, (tampleY + i) * GS, GS, GS);
					} else
						break;
				} else
					break;

			}
			for (int i = 1;; i++) {
				if (i <= 14 - tampleX && i <= tampleY) {
					if (board[tampleY - i][tampleX + i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX + i) * GS, (tampleY - i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= tampleX && i <= 14 - tampleY) {
					if (board[tampleY + i][tampleX - i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX - i) * GS, (tampleY + i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= tampleX && i <= tampleY) {
					if (board[tampleY - i][tampleX - i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX - i) * GS, (tampleY - i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}

			break;

		case 9:

			if (tampleX + 1 <= 14 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			break;

		case 10:
			if (tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0) {
				if (board[tampleY][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, tampleY * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14) {
				if (board[tampleY][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, tampleY * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			break;

		case 11:
			if (tampleX + 1 <= 14 && tampleY - 2 >= 0) {
				if (board[tampleY - 2][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY - 2) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY - 2 >= 0) {
				if (board[tampleY - 2][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY - 2) * GS, GS, GS);
				}
			}

			if (tampleX + 2 <= 14 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX + 2] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 2) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 2 >= 0 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX - 2] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 2) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX + 2 <= 14 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX + 2] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 2) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX - 2 >= 0 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX - 2] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 2) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14 && tampleY + 2 <= 14) {
				if (board[tampleY + 2][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY + 2) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY + 2 <= 14) {
				if (board[tampleY + 2][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY + 2) * GS, GS, GS);
				}
			}

			break;

		case 12:
			for (int i = tampleX + 1; i <= 14; i++) {
				if (board[tampleY][i] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(i * GS, tampleY * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleX - 1; i >= 0; i--) {
				if (board[tampleY][i] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(i * GS, tampleY * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleY + 1; i <= 14; i++) {
				if (board[i][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, i * GS, GS, GS);
				} else
					break;
			}
			for (int i = tampleY - 1; i >= 0; i--) {
				if (board[i][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, i * GS, GS, GS);
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= 14 - tampleX && i <= 14 - tampleY) {
					if (board[tampleY + i][tampleX + i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX + i) * GS, (tampleY + i) * GS, GS, GS);
					} else
						break;
				} else
					break;

			}
			for (int i = 1;; i++) {
				if (i <= 14 - tampleX && i <= tampleY) {
					if (board[tampleY - i][tampleX + i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX + i) * GS, (tampleY - i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= tampleX && i <= 14 - tampleY) {
					if (board[tampleY + i][tampleX - i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX - i) * GS, (tampleY + i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}
			for (int i = 1;; i++) {
				if (i <= tampleX && i <= tampleY) {
					if (board[tampleY - i][tampleX - i] == BLANK) {
						g.setColor(new Color(255, 0, 0, 50));
						g.fillRect((tampleX - i) * GS, (tampleY - i) * GS, GS, GS);
					} else
						break;
				} else
					break;
			}

			break;

		case 13:
			if (tampleX + 1 <= 14 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleY + 1 <= 14) {
				if (board[tampleY + 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY + 1) * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0 && tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleY - 1 >= 0) {
				if (board[tampleY - 1][tampleX] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect(tampleX * GS, (tampleY - 1) * GS, GS, GS);
				}
			}

			if (tampleX - 1 >= 0) {
				if (board[tampleY][tampleX - 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX - 1) * GS, tampleY * GS, GS, GS);
				}
			}

			if (tampleX + 1 <= 14) {
				if (board[tampleY][tampleX + 1] == BLANK) {
					g.setColor(new Color(255, 0, 0, 50));
					g.fillRect((tampleX + 1) * GS, tampleY * GS, GS, GS);
				}
			}

			break;

		default:
			break;

		}

	}

	public void drawChara(Graphics g) {
		for (int j = 0; j < MASU; j++) {
			for (int i = 0; i < MASU; i++) {
				if (board[j][i] != -1) {
					g.drawImage(charaImage[board[j][i]], i * GS + 1, j * GS + 1, 38, 38, null);
				}
			}
		}
	}

	public void loadImage() {
		// キャラクターのイメージをロード
		for (int i = 0; i <= 13; i++) {
			String jpg = "img/" + i + ".png";
			ImageIcon icon = new ImageIcon(getClass().getResource(jpg));
			charaImage[i] = icon.getImage();
		}
	}

	/**
	 * 画面の中央に文字列を表示する
	 *
	 * @param g
	 *            描画オブジェクト
	 * @param s
	 *            描画したい文字列
	 */
	public void drawTextCentering(Graphics g, String s) {
		Font f = new Font("SansSerif", Font.BOLD, 50);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.BLACK);
		g.drawString(s, WIDTH / 2 - fm.stringWidth(s) / 2, HEIGHT / 2 + fm.getDescent());
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

}
