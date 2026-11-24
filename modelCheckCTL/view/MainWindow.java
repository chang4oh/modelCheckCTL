package modelCheckCTL.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

/** Standalone Swing interface; all model-checking logic stays outside the view. */
public final class MainWindow extends JFrame {
    private final JTextField filename = new JTextField();
    private final JButton browse = new JButton("Open model...");
    private final JComboBox<String> state = new JComboBox<>();
    private final JTextField formula = new JTextField("AG(t1 -> AF c1)");
    private final JButton check = new JButton("Check formula");
    private final JTextArea description = new JTextArea(10, 36);
    private final JTextArea output = new JTextArea(5, 36);

    public MainWindow() {
        super("CTL Model Checker | CS 5392");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 565));
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(18,18,18,18));
        setContentPane(root);
        JLabel title = new JLabel("CTL Model Checker");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 23f));
        root.add(title, BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(10,10));
        root.add(content,BorderLayout.CENTER);
        JPanel inputs = new JPanel(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(6,2,6,7); g.anchor=GridBagConstraints.WEST;
        g.gridx=0;g.gridy=0;inputs.add(new JLabel("Kripke model"),g);
        g.gridx=1;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;
        filename.setEditable(false);inputs.add(filename,g);
        g.gridx=2;g.weightx=0;inputs.add(browse,g);
        g.gridx=0;g.gridy=1;inputs.add(new JLabel("State ID"),g);
        g.gridx=1;g.gridwidth=2;g.fill=GridBagConstraints.HORIZONTAL;inputs.add(state,g);
        g.gridx=0;g.gridy=2;g.gridwidth=1;inputs.add(new JLabel("CTL formula"),g);
        g.gridx=1;g.gridwidth=2;inputs.add(formula,g);
        g.gridx=1;g.gridy=3;g.gridwidth=2;g.fill=GridBagConstraints.NONE;g.anchor=GridBagConstraints.EAST;inputs.add(check,g);
        content.add(inputs,BorderLayout.NORTH);
        description.setEditable(false);description.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
        description.setLineWrap(false);
        output.setEditable(false); output.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));
        output.setBackground(new Color(241,247,252));output.setBorder(new EmptyBorder(10,10,10,10));
        JPanel panels=new JPanel(new GridLayout(2,1,8,8));
        JPanel modelPanel=new JPanel(new BorderLayout(3,3));modelPanel.add(new JLabel("Loaded model"),BorderLayout.NORTH);
        modelPanel.add(new JScrollPane(description),BorderLayout.CENTER);panels.add(modelPanel);
        JPanel resultPanel=new JPanel(new BorderLayout(3,3));resultPanel.add(new JLabel("Result / satisfying states"),BorderLayout.NORTH);
        resultPanel.add(new JScrollPane(output),BorderLayout.CENTER);panels.add(resultPanel);
        content.add(panels,BorderLayout.CENTER);
        JMenuBar bar=new JMenuBar();JMenu file=new JMenu("File");JMenuItem open=new JMenuItem("Open Kripke file");
        open.addActionListener(e -> browse.doClick());file.add(open);bar.add(file);setJMenuBar(bar);
        output.setText("Open a Kripke model, select a state, and enter a CTL formula.");
        pack();setLocationRelativeTo(null);
    }
    public void onOpen(Runnable handler) { browse.addActionListener(e -> handler.run()); }
    public void onCheck(Runnable handler) { check.addActionListener(e -> handler.run()); formula.addActionListener(e -> handler.run()); }
    public Path chooseFile() {
        JFileChooser chooser = new JFileChooser(); chooser.setDialogTitle("Choose Kripke model (.txt)");
        return chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile().toPath() : null;
    }
    public void setModel(Path path, Collection<String> stateIds, String summary) {
        filename.setText(path.toAbsolutePath().toString());state.removeAllItems();
        for (String id:stateIds) state.addItem(id);
        description.setText(summary);description.setCaretPosition(0);
        output.setText("Model loaded successfully. Choose a state and press Check formula.");
    }
    /** Test harness helper: operates through the same fields and button as a user. */
    public void setInputs(String stateId, String expression) { state.setSelectedItem(stateId); formula.setText(expression); }
    public void triggerCheck() { check.doClick(); }
    public String currentState() { return (String) state.getSelectedItem(); }
    public String currentFormula() { return formula.getText(); }
    public void setOutput(String message) { output.setText(message); }
    public void showError(String message) { JOptionPane.showMessageDialog(this,message,"CTL Model Checker - Error",JOptionPane.ERROR_MESSAGE); }
}
