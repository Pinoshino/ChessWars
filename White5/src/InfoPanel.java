import javax.swing.*;

/**
 * @author mori
 *
 */
public class InfoPanel extends JPanel {
	private JLabel charaLabel1;
	private JLabel charaLabel2;
	private JLabel charaLabel3;

	public InfoPanel() {
		add(new JLabel("Chara:"));
		charaLabel1 = new JLabel("noName");
		add(charaLabel1);
		add(new JLabel("HP:"));
		charaLabel2 = new JLabel("-1");
		add(charaLabel2);
		add(new JLabel("AP:"));
		charaLabel3 = new JLabel("-1");
		add(charaLabel3);
	}

	/**
	 * BLACK繝ｩ繝吶Ν縺ｫ蛟､繧偵そ繝�繝医☆繧九��
	 *
	 * @param count
	 *            繧ｻ繝�繝医☆繧区焚蟄励��
	 *
	 */
	public void setCharaLabel(String name, int hp, int ap) {
		charaLabel1.setText(name + "");
		charaLabel2.setText(hp + "");
		charaLabel3.setText(ap + "");

	}

	/**
	 * WHITE繝ｩ繝吶Ν縺ｫ蛟､繧偵そ繝�繝医☆繧九��
	 *
	 * @param text
	 *            繧ｻ繝�繝医☆繧区焚蟄励��
	 *
	 *
	 *            public void setWhiteLabel(int count) {
	 *            whiteLabel.setText(count + ""); }
	 */
}
