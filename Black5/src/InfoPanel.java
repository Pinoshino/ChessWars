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


	public void setCharaLabel(String name, int hp, int ap) {
		charaLabel1.setText(name + "");
		charaLabel2.setText(hp + "");
		charaLabel3.setText(ap + "");

	}

}
