import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class project3 extends JFrame {
    private JTextPane terminal;
    private JTextField commandInput;
    private StyledDocument doc;

    public project3() {
        setTitle("EMSQUAD - KERNEL TERMINAL v4.0");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Main Terminal Panel
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(10, 10, 10));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Hacker Watermark
                g2d.setFont(new Font("Consolas", Font.BOLD, 150));
                g2d.setColor(new Color(0, 255, 0, 15));
                g2d.drawString("EMSQUAD", 50, getHeight() / 2);
            }
        };

        // Terminal Output Area
        terminal = new JTextPane();
        terminal.setBackground(new Color(0, 0, 0, 0)); // Transparent to show watermark
        terminal.setOpaque(false);
        terminal.setEditable(false);
        terminal.setFont(new Font("Consolas", Font.PLAIN, 16));
        doc = terminal.getStyledDocument();
        
        JScrollPane scroll = new JScrollPane(terminal);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // Command Input Field (The CLI look)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.BLACK);
        
        JLabel prompt = new JLabel(" EMSQUAD@ROOT:~# ");
        prompt.setForeground(Color.GREEN);
        prompt.setFont(new Font("Consolas", Font.BOLD, 16));
        
        commandInput = new JTextField();
        commandInput.setBackground(Color.BLACK);
        commandInput.setForeground(Color.WHITE);
        commandInput.setCaretColor(Color.GREEN);
        commandInput.setFont(new Font("Consolas", Font.PLAIN, 16));
        commandInput.setBorder(null);
        
        inputPanel.add(prompt, BorderLayout.WEST);
        inputPanel.add(commandInput, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Initial Boot Sequence
        bootSequence();

        // Command Listener
        commandInput.addActionListener(e -> {
            String cmd = commandInput.getText().trim();
            processCommand(cmd);
            commandInput.setText("");
        });
    }

    private void append(String msg, Color c) {
        Style style = terminal.addStyle("ColorStyle", null);
        StyleConstants.setForeground(style, c);
        try { doc.insertString(doc.getLength(), msg, style); } catch (Exception e) {}
        terminal.setCaretPosition(doc.getLength());
    }

    private void bootSequence() {
        append(">>> EMSQUAD OS [Version 4.0.26]\n", Color.GREEN);
        append(">>> (c) 2026 EMSQUAD SECURITY. ALL RIGHTS RESERVED.\n", Color.GREEN);
        append(">>> TYPE 'help' TO LIST COMMANDS.\n\n", Color.YELLOW);
    }

    private void processCommand(String cmd) {
        append("EMSQUAD@ROOT:~# " + cmd + "\n", Color.WHITE);
        
        if (cmd.startsWith("scan ")) {
            String target = cmd.substring(5);
            startScan(target);
        } else if (cmd.equals("save")) {
            saveReport();
        } else if (cmd.equals("clear")) {
            terminal.setText("");
            bootSequence();
        } else if (cmd.equals("help")) {
            append("AVAILABLE COMMANDS:\n", Color.CYAN);
            append("  scan [target]  - Start deep audit on IP/Domain\n", Color.WHITE);
            append("  save           - Export terminal log to .txt\n", Color.WHITE);
            append("  clear          - Clear terminal screen\n", Color.WHITE);
            append("  exit           - Shutdown terminal\n", Color.WHITE);
        } else if (cmd.equals("exit")) {
            System.exit(0);
        } else {
            append("[!] ERROR: COMMAND NOT FOUND: " + cmd + "\n", Color.RED);
        }
    }

    private void startScan(String target) {
        new Thread(() -> {
            append(">>> [EMSQUAD] INITIATING PACKET INSPECTION: " + target + "\n", Color.YELLOW);
            try {
                InetAddress addr = InetAddress.getByName(target);
                append("[+] TARGET ACQUIRED: " + addr.getHostAddress() + "\n", Color.GREEN);
                
                // TTL OS Check
                Process p = Runtime.getRuntime().exec("ping -n 1 " + target);
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("TTL=")) {
                        int ttl = Integer.parseInt(line.split("TTL=")[1].split(" ")[0].trim());
                        String os = (ttl <= 64) ? "Linux" : "Windows";
                        append("[+] OS DETECTED: " + os + " (TTL=" + ttl + ")\n", new Color(0, 200, 0));
                    }
                }

                // Port Check (Fast)
                int[] ports = {80, 443, 3306, 5555};
                for (int port : ports) {
                    try (Socket s = new Socket()) {
                        s.connect(new InetSocketAddress(target, port), 300);
                        append("    [!] ALERT: PORT " + port + " OPEN\n", Color.RED);
                    } catch (Exception ex) {}
                }
                append(">>> SCAN COMPLETED.\n", Color.CYAN);
            } catch (Exception e) {
                append("[!] FAILED TO RESOLVE HOST.\n", Color.RED);
            }
        }).start();
    }

    private void saveReport() {
        try (PrintWriter out = new PrintWriter("EMSQUAD_LOG.txt")) {
            out.println(terminal.getText());
            append(">>> [SUCCESS] LOG SAVED TO EMSQUAD_LOG.TXT\n", Color.GREEN);
        } catch (Exception e) {
            append("[!] SAVE FAILED.\n", Color.RED);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new project3().setVisible(true));
    }
}