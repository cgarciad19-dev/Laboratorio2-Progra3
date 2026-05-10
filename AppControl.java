import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

/**
 * PC 1 — APLICACIÓN DE CONTROL
 * Diseño simple y limpio. Solo JDK estándar.
 *
 * Paso 1 → IP de PC 2 → leer archivo
 * Paso 2 → Revisar contenido
 * Paso 3 → IP de PC 3 → enviar
 */
public class AppControl extends JFrame {

    private static final int PUERTO_ORIGEN  = 5001;
    private static final int PUERTO_DESTINO = 5002;

    private List<String> contenido = new ArrayList<>();
    private int paso = 1;

    // Colores simples
    private static final Color FONDO    = Color.WHITE;
    private static final Color GRIS_LT  = new Color(245, 245, 245);
    private static final Color BORDE    = new Color(220, 220, 220);
    private static final Color TEXTO    = new Color(30, 30, 30);
    private static final Color MUTED    = new Color(120, 120, 120);
    private static final Color AZUL     = new Color(37, 99, 235);
    private static final Color VERDE    = new Color(22, 163, 74);
    private static final Color ROJO     = new Color(220, 38, 38);

    // Componentes
    private JTextField tfIpOrigen, tfIpDestino;
    private JTextArea  areaContenido, areaLog;
    private JButton    btnAccion, btnReset;
    private JLabel     lblTituloPaso, lblDescPaso, lblEstado;
    private JPanel[]   indicadores = new JPanel[3];
    private JPanel     panelIndicadoresRef;
    private CardLayout cards;
    private JPanel     cardPanel;

    public AppControl() {
        super("Transferencia de Archivos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(580, 560);
        setMinimumSize(new Dimension(480, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        add(panelTitulo(),   BorderLayout.NORTH);
        add(panelCentro(),   BorderLayout.CENTER);
        add(panelInferior(), BorderLayout.SOUTH);

        actualizarIndicadores();
    }

    // ─── Título superior ────────────────────────────────────────────────────
    private JPanel panelTitulo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(FONDO);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDE),
            new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel titulo = new JLabel("Transferencia de Archivos por Red");
        titulo.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        titulo.setForeground(TEXTO);

        lblEstado = new JLabel("Paso 1 de 3");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEstado.setForeground(MUTED);

        p.add(titulo,    BorderLayout.WEST);
        p.add(lblEstado, BorderLayout.EAST);
        return p;
    }

    // ─── Panel central ───────────────────────────────────────────────────────
    private JPanel panelCentro() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(FONDO);
        p.setBorder(new EmptyBorder(20, 20, 0, 20));

        p.add(panelIndicadores(), BorderLayout.NORTH);
        p.add(panelContenido(),   BorderLayout.CENTER);

        return p;
    }

    // ─── Indicadores de paso (tres puntos/números) ──────────────────────────
    private JPanel panelIndicadores() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(FONDO);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));
        panelIndicadoresRef = p;

        String[] nombres = {"Conectar a PC 2", "Revisar contenido", "Enviar a PC 3"};

        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                JLabel sep = new JLabel("  ——  ");
                sep.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                sep.setForeground(BORDE);
                p.add(sep);
            }

            final int idx = i;
            JPanel slot = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            slot.setBackground(FONDO);

            JPanel circulo = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (paso > idx + 1) {
                        g2.setColor(VERDE);
                        g2.fillOval(0, 0, 22, 22);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        g2.drawString("✓", 5, 16);
                    } else if (paso == idx + 1) {
                        g2.setColor(AZUL);
                        g2.fillOval(0, 0, 22, 22);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        String n = String.valueOf(idx + 1);
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(n, (22 - fm.stringWidth(n)) / 2, 15);
                    } else {
                        g2.setColor(BORDE);
                        g2.fillOval(0, 0, 22, 22);
                        g2.setColor(MUTED);
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        String n = String.valueOf(idx + 1);
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(n, (22 - fm.stringWidth(n)) / 2, 15);
                    }
                }
                @Override public Dimension getPreferredSize() { return new Dimension(22, 22); }
            };
            circulo.setOpaque(false);
            indicadores[i] = circulo;

            JLabel lbl = new JLabel(nombres[i]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(paso == i + 1 ? TEXTO : MUTED);

            slot.add(circulo);
            slot.add(lbl);
            p.add(slot);
        }
        return p;
    }

    // ─── Contenido de cada paso (CardLayout) ─────────────────────────────────
    private JPanel panelContenido() {
        cards = new CardLayout();
        cardPanel = new JPanel(cards);
        cardPanel.setBackground(FONDO);
        cardPanel.add(buildPaso1(), "paso1");
        cardPanel.add(buildPaso2(), "paso2");
        cardPanel.add(buildPaso3(), "paso3");
        return cardPanel;
    }

    // ─── Paso 1 ──────────────────────────────────────────────────────────────
    private JPanel buildPaso1() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(FONDO);

        p.add(subtitulo("IP de PC 2 (Servidor Origen)"));
        p.add(Box.createVerticalStrut(4));
        p.add(desc("Ingresa la IP de la computadora que tiene el archivo de nombres.\nAsegúrate que ServidorOrigen.java ya esté corriendo."));
        p.add(Box.createVerticalStrut(14));
        p.add(etiqueta("Dirección IP:"));
        p.add(Box.createVerticalStrut(6));
        tfIpOrigen = campo("192.168.1.100");
        p.add(tfIpOrigen);
        p.add(Box.createVerticalStrut(16));
        btnAccion = botonPrimario("Conectar y leer archivo");
        btnAccion.addActionListener(e -> doPaso1());
        p.add(btnAccion);
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ─── Paso 2 ──────────────────────────────────────────────────────────────
    private JPanel buildPaso2() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(FONDO);

        p.add(subtitulo("Contenido recibido desde PC 2"));
        p.add(Box.createVerticalStrut(4));
        p.add(desc("Revisa los datos antes de enviarlos. Si todo está correcto, continúa."));
        p.add(Box.createVerticalStrut(12));

        areaContenido = new JTextArea();
        areaContenido.setEditable(false);
        areaContenido.setFont(new Font("Consolas", Font.PLAIN, 13));
        areaContenido.setBackground(GRIS_LT);
        areaContenido.setForeground(TEXTO);
        areaContenido.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane scroll = new JScrollPane(areaContenido);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        scroll.setPreferredSize(new Dimension(400, 170));
        p.add(scroll);

        p.add(Box.createVerticalStrut(16));
        JButton btnOk = botonPrimario("Continuar al paso 3");
        btnOk.addActionListener(e -> irA(3));
        p.add(btnOk);
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ─── Paso 3 ──────────────────────────────────────────────────────────────
    private JPanel buildPaso3() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(FONDO);

        p.add(subtitulo("IP de PC 3 (Servidor Destino)"));
        p.add(Box.createVerticalStrut(4));
        p.add(desc("Ingresa la IP de la computadora donde se guardará el archivo.\nAsegúrate que ServidorDestino.java ya esté corriendo."));
        p.add(Box.createVerticalStrut(14));
        p.add(etiqueta("Dirección IP:"));
        p.add(Box.createVerticalStrut(6));
        tfIpDestino = campo("192.168.1.101");
        p.add(tfIpDestino);
        p.add(Box.createVerticalStrut(16));
        JButton btnEnviar = botonPrimario("Enviar archivo a PC 3");
        btnEnviar.addActionListener(e -> doPaso3());
        p.add(btnEnviar);
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ─── Panel inferior (log) ─────────────────────────────────────────────────
    private JPanel panelInferior() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(FONDO);
        p.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDE),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(FONDO);

        JLabel lblLog = new JLabel("Actividad");
        lblLog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLog.setForeground(MUTED);

        btnReset = new JButton("Reiniciar");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setForeground(ROJO);
        btnReset.setBackground(FONDO);
        btnReset.setBorder(BorderFactory.createLineBorder(ROJO));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(new Dimension(90, 28));
        btnReset.addActionListener(e -> reset());

        fila.add(lblLog,    BorderLayout.WEST);
        fila.add(btnReset,  BorderLayout.EAST);

        areaLog = new JTextArea(4, 0);
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 11));
        areaLog.setBackground(GRIS_LT);
        areaLog.setForeground(MUTED);
        areaLog.setBorder(new EmptyBorder(6, 10, 6, 10));
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));

        p.add(fila,   BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    //  LÓGICA
    // ═══════════════════════════════════════════════════════════════

    private void doPaso1() {
        String ip = leerCampo(tfIpOrigen);
        if (ip == null) { alerta("Ingresa la IP de PC 2."); return; }

        btnAccion.setEnabled(false);
        btnAccion.setText("Conectando...");
        log("Conectando a PC 2 (" + ip + ":" + PUERTO_ORIGEN + ")...");

        new SwingWorker<List<String>, String>() {
            @Override protected List<String> doInBackground() throws Exception {
                List<String> lineas = new ArrayList<>();
                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress(ip, PUERTO_ORIGEN), 5000);
                    publish("Conexión con PC 2 OK");
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String hs = in.readLine();
                    if (hs == null || !hs.startsWith("INICIO:"))
                        throw new IOException("Respuesta inesperada: " + hs);
                    long total = Long.parseLong(hs.split(":")[1]);
                    publish("El archivo tiene " + total + " registros");
                    String linea;
                    while ((linea = in.readLine()) != null) {
                        if ("FIN".equals(linea)) break;
                        lineas.add(linea);
                    }
                    publish("Lectura completada: " + lineas.size() + " registros");
                }
                return lineas;
            }
            @Override protected void process(List<String> c) { c.forEach(AppControl.this::log); }
            @Override protected void done() {
                try {
                    contenido = get();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < contenido.size(); i++)
                        sb.append(String.format("%3d.  %s%n", i + 1, contenido.get(i)));
                    areaContenido.setText(sb.toString());
                    log("Paso 1 completado.");
                    irA(2);
                } catch (Exception ex) {
                    manejarError(ex, "PC 2");
                    btnAccion.setEnabled(true);
                    btnAccion.setText("Conectar y leer archivo");
                }
            }
        }.execute();
    }

    private void doPaso3() {
        if (contenido.isEmpty()) { alerta("No hay contenido. Regresa al Paso 1."); return; }
        String ip = leerCampo(tfIpDestino);
        if (ip == null) { alerta("Ingresa la IP de PC 3."); return; }

        log("Conectando a PC 3 (" + ip + ":" + PUERTO_DESTINO + ")...");
        final List<String> datos = new ArrayList<>(contenido);

        new SwingWorker<String, String>() {
            @Override protected String doInBackground() throws Exception {
                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress(ip, PUERTO_DESTINO), 5000);
                    publish("Conexión con PC 3 OK");
                    PrintWriter   out = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    out.println("INICIO:" + datos.size());
                    String resp = in.readLine();
                    if (resp == null || !resp.startsWith("OK"))
                        throw new IOException("PC 3 rechazó la conexión: " + resp);
                    publish("Enviando " + datos.size() + " registros...");
                    for (String d : datos) out.println(d);
                    out.println("FIN");
                    String conf = in.readLine();
                    if (conf != null && conf.startsWith("OK:TRANSFERENCIA_COMPLETA")) {
                        String[] parts = conf.split(":");
                        return parts.length > 2 ? parts[2] : String.valueOf(datos.size());
                    }
                    throw new IOException("Confirmación inesperada: " + conf);
                }
            }
            @Override protected void process(List<String> c) { c.forEach(AppControl.this::log); }
            @Override protected void done() {
                try {
                    String total = get();
                    log("Transferencia completada: " + total + " registros escritos en PC 3.");
                    lblEstado.setText("¡Completado!");
                    JOptionPane.showMessageDialog(AppControl.this,
                        total + " registros transferidos correctamente.\n" +
                        "El archivo fue guardado en PC 3 como destino.txt.",
                        "Transferencia exitosa", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    manejarError(ex, "PC 3");
                }
            }
        }.execute();
    }

    private void irA(int p) {
        paso = p;
        cards.show(cardPanel, "paso" + p);
        lblEstado.setText("Paso " + p + " de 3");
        actualizarIndicadores();
    }

    private void reset() {
        contenido.clear();
        areaContenido.setText("");
        areaLog.setText("");
        tfIpOrigen.setText("");
        tfIpDestino.setText("");
        btnAccion.setEnabled(true);
        btnAccion.setText("Conectar y leer archivo");
        log("Proceso reiniciado.");
        irA(1);
    }

    private void manejarError(Exception ex, String pc) {
        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        log("Error: " + msg);
        JOptionPane.showMessageDialog(this,
            "No se pudo conectar a " + pc + ":\n" + msg +
            "\n\n• Verifica que el servidor esté corriendo\n• Comprueba que la IP sea correcta\n• Confirma que ambas PCs estén en la misma red",
            "Error de conexión", JOptionPane.ERROR_MESSAGE);
    }

    private void actualizarIndicadores() {
        for (JPanel ind : indicadores) if (ind != null) ind.repaint();
        // Actualizar etiquetas: recorrer el panel de indicadores guardado
        if (panelIndicadoresRef != null) {
            int idx = 0;
            for (Component c : panelIndicadoresRef.getComponents()) {
                if (c instanceof JPanel slot) {
                    for (Component sc : ((JPanel) slot).getComponents()) {
                        if (sc instanceof JLabel lbl) {
                            lbl.setForeground(paso == idx + 1 ? TEXTO : MUTED);
                        }
                    }
                    idx++;
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ═══════════════════════════════════════════════════════════════

    private JLabel subtitulo(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        l.setForeground(TEXTO);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel desc(String t) {
        JLabel l = new JLabel("<html>" + t.replace("\n", "<br>") + "</html>");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel etiqueta(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXTO);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField campo(String ph) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Consolas", Font.PLAIN, 14));
        tf.setForeground(TEXTO);
        tf.setBackground(FONDO);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE),
            new EmptyBorder(7, 10, 7, 10)));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setToolTipText("Ejemplo: " + ph);
        return tf;
    }

    private JButton botonPrimario(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(AZUL);
        b.setBorder(new EmptyBorder(9, 20, 9, 20));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.addMouseListener(new MouseAdapter() {
            Color base = AZUL;
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(base.darker()); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(base); }
        });
        return b;
    }

    private String leerCampo(JTextField tf) {
        String v = tf.getText().trim();
        return v.isEmpty() ? null : v;
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(msg + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    private void alerta(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new AppControl().setVisible(true));
    }
}