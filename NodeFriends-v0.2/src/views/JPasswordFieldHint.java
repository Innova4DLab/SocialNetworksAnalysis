
package views;

import static com.sun.java.accessibility.util.AWTEventMonitor.addFocusListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JPasswordField;
 
/**
 * @author playscode - Norman Carcamo.
 */
public class JPasswordFieldHint extends JPasswordField implements FocusListener {
 
    private final Font fontLost = new Font("Monaco", Font.ITALIC, 10);
    private final Font fontGained = new Font("Monaco", Font.PLAIN, 12);
    private final Color colorLost = Color.LIGHT_GRAY;
    private final Color colorGained = Color.BLACK;
    private String hint;
 
    @SuppressWarnings("LeakingThisInConstructor")
    public JPasswordFieldHint() {
        addFocusListener(this);
    }
 
    public void setHint(String hint) {
        
        setForeground(colorLost);
        setFont(fontLost);
        setText(hint);
        this.hint = hint;
    }
 
    public String getHint() {
        return hint;
    }
 
    @Override
    public void focusGained(FocusEvent e) {
        if (getText().equals(getHint())) {
            setText("");
            setFont(fontGained);
            setForeground(colorGained);
        } else {
            setForeground(colorGained);
            setFont(fontGained);
            setText(getText());
        }
    }
 
    @Override
    public void focusLost(FocusEvent e) {
        if (getText().length() <= 0) {
            setHint(getHint());
        } else {
            setForeground(colorGained);
            setFont(fontGained);
            setText(getText());
        }
    }
}