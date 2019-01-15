import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class GUI extends JFrame {

	private JPanel contentPane;
	public JButton btnNewButton;
	public JComboBox<String> comboBox;

	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 148, 108);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(ae -> setVisible(false));
		btnNewButton.setBounds(20, 42, 89, 23);
		contentPane.add(btnNewButton);

		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] { "Willow", "Yew" }));
		comboBox.setBounds(10, 11, 121, 20);
		contentPane.add(comboBox);
	}
}