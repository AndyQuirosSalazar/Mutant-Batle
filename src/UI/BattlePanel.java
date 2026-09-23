package UI;

import Game.Battlefield;
import Model.Mutant;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class BattlePanel extends JPanel {

    public static final Color TEAM1_COLOR = new Color(66, 135, 245); // color del equipo 1
    public static final Color TEAM2_COLOR = new Color(235, 77, 75); // color del equipo 2
    public static final Color DEAD_COLOR = new Color(120, 120, 120); // color de los mutantes muertos

    private static final Color BACKGROUND_COLOR = new Color(28, 33, 40);
    private static final Color ARENA_COLOR = new Color(38, 45, 54);
    private static final Color GRID_COLOR = new Color(52, 60, 71);
    private static final int MUTANT_SIZE = 24; // diámetro en pixeles de cada mutante
    private static final int MARGIN = 40; // espacio entre el borde del panel y la arena
    private static final int GRID_STEP = 40;

    private Battlefield battlefield; // solo se consulta, nunca se modifica
    private final Map<Mutant, Integer> initialEnergy = new IdentityHashMap<>(); // energía con la que empezó cada mutante
    private int worldWidth = 1; // mayor coordenada x observada, para escalar al tamaño del panel
    private int worldHeight = 1; // mayor coordenada y observada, para escalar al tamaño del panel
    private String banner; // mensaje de ganador dibujado sobre la arena

    public BattlePanel() {
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(850, 700));
    }

    public void setBattlefield(Battlefield battlefield) {
        // Guarda la referencia a la nueva partida y la energía inicial de cada mutante
        this.battlefield = battlefield;
        this.banner = null;
        this.worldWidth = 1;
        this.worldHeight = 1;
        initialEnergy.clear();
        for (Mutant mutant : battlefield.getAllMutants()) {
            initialEnergy.put(mutant, Math.max(mutant.energy, 1));
        }
        repaint();
    }

    public void showBanner(String text) {
        this.banner = text;
        repaint();
    }

    public static Color teamColor(Battlefield battlefield, Mutant mutant) {
        // Color que identifica al equipo del mutante (compartido con ScoreboardPanel)
        return mutant.team == battlefield.team1.name ? TEAM1_COLOR : TEAM2_COLOR;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Método nativo de Java donde se programa el renderizado gráfico
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBattlefield(g2);

        if (battlefield == null) {
            drawCenteredText(g2, "Ingresa la cantidad de mutantes y presiona \"Nueva partida\"", Color.LIGHT_GRAY, 16);
        } else {
            List<Mutant> mutants = battlefield.getAllMutants();
            updateWorldBounds(mutants);
            // Primero los muertos, para que los vivos queden dibujados encima
            for (Mutant mutant : mutants) {
                if (!mutant.isAlive) {
                    drawMutant(g2, mutant);
                }
            }
            for (Mutant mutant : mutants) {
                if (mutant.isAlive) {
                    drawMutant(g2, mutant);
                }
            }
            drawLegend(g2);
        }

        if (banner != null) {
            drawBanner(g2);
        }
        g2.dispose();
    }

    private void drawBattlefield(Graphics2D g2) {
        // Renderiza el fondo usando las dimensiones del CampoDeBatalla
        int arenaWidth = getWidth() - 2 * MARGIN;
        int arenaHeight = getHeight() - 2 * MARGIN;

        g2.setColor(ARENA_COLOR);
        g2.fillRoundRect(MARGIN - 10, MARGIN - 10, arenaWidth + 20, arenaHeight + 20, 16, 16);

        g2.setColor(GRID_COLOR);
        for (int x = MARGIN; x <= MARGIN + arenaWidth; x += GRID_STEP) {
            g2.drawLine(x, MARGIN, x, MARGIN + arenaHeight);
        }
        for (int y = MARGIN; y <= MARGIN + arenaHeight; y += GRID_STEP) {
            g2.drawLine(MARGIN, y, MARGIN + arenaWidth, y);
        }
    }

    private void drawMutant(Graphics2D g2, Mutant mutant) {
        // Dibuja la forma, el color del equipo, el símbolo y la barra de energía en las coordenadas x, y actuales
        int centerX = toScreenX(mutant.x);
        int centerY = toScreenY(mutant.y);
        int left = centerX - MUTANT_SIZE / 2;
        int top = centerY - MUTANT_SIZE / 2;

        if (!mutant.isAlive) {
            g2.setColor(new Color(DEAD_COLOR.getRed(), DEAD_COLOR.getGreen(), DEAD_COLOR.getBlue(), 110));
            g2.fillOval(left, top, MUTANT_SIZE, MUTANT_SIZE);
            g2.setColor(DEAD_COLOR);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(left + 6, top + 6, left + MUTANT_SIZE - 6, top + MUTANT_SIZE - 6);
            g2.drawLine(left + MUTANT_SIZE - 6, top + 6, left + 6, top + MUTANT_SIZE - 6);
            return;
        }

        Color color = teamColor(battlefield, mutant);
        g2.setColor(color);
        g2.fillOval(left, top, MUTANT_SIZE, MUTANT_SIZE);
        g2.setColor(color.brighter());
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(left, top, MUTANT_SIZE, MUTANT_SIZE);

        // Símbolo: número del equipo dentro del círculo
        String symbol = String.valueOf((int) mutant.team);
        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        FontMetrics metrics = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(symbol, centerX - metrics.stringWidth(symbol) / 2,
                centerY + (metrics.getAscent() - metrics.getDescent()) / 2);

        // Barra de energía sobre el mutante
        int barWidth = MUTANT_SIZE + 8;
        int barHeight = 5;
        int barLeft = centerX - barWidth / 2;
        int barTop = top - barHeight - 4;
        double ratio = Math.max(0, Math.min(1, mutant.energy / (double) initialEnergy.getOrDefault(mutant, 1)));

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barLeft, barTop, barWidth, barHeight);
        g2.setColor(energyColor(ratio));
        g2.fillRect(barLeft, barTop, (int) Math.round(barWidth * ratio), barHeight);
    }

    private void drawLegend(Graphics2D g2) {
        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        int y = MARGIN - 18;
        g2.setColor(TEAM1_COLOR);
        g2.fillOval(MARGIN, y - 10, 12, 12);
        g2.drawString("Equipo " + (int) battlefield.team1.name, MARGIN + 18, y);
        g2.setColor(TEAM2_COLOR);
        g2.fillOval(MARGIN + 110, y - 10, 12, 12);
        g2.drawString("Equipo " + (int) battlefield.team2.name, MARGIN + 128, y);
    }

    private void drawBanner(Graphics2D g2) {
        int bannerHeight = 70;
        int top = getHeight() / 2 - bannerHeight / 2;
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, top, getWidth(), bannerHeight);
        drawCenteredText(g2, banner, Color.WHITE, 24);
    }

    private void drawCenteredText(Graphics2D g2, String text, Color color, float size) {
        g2.setFont(getFont().deriveFont(Font.BOLD, size));
        FontMetrics metrics = g2.getFontMetrics();
        g2.setColor(color);
        g2.drawString(text, (getWidth() - metrics.stringWidth(text)) / 2,
                getHeight() / 2 + (metrics.getAscent() - metrics.getDescent()) / 2);
    }

    private void updateWorldBounds(List<Mutant> mutants) {
        // La escala solo crece, para que el campo no "salte" de tamaño entre repintados
        for (Mutant mutant : mutants) {
            worldWidth = Math.max(worldWidth, mutant.x);
            worldHeight = Math.max(worldHeight, mutant.y);
        }
    }

    private int toScreenX(int x) {
        return MARGIN + (int) Math.round(x * (getWidth() - 2.0 * MARGIN) / worldWidth);
    }

    private int toScreenY(int y) {
        return MARGIN + (int) Math.round(y * (getHeight() - 2.0 * MARGIN) / worldHeight);
    }

    private static Color energyColor(double ratio) {
        if (ratio > 0.6) {
            return new Color(76, 209, 55);
        }
        if (ratio > 0.3) {
            return new Color(251, 197, 49);
        }
        return new Color(232, 65, 24);
    }
}
