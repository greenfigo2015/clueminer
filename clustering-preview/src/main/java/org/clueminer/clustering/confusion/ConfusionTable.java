package org.clueminer.clustering.confusion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.colors.ColorScheme;
import org.clueminer.dataset.api.Instance;
import org.clueminer.gui.ColorPalette;

/**
 *
 * @author Tomas Barton
 */
public class ConfusionTable extends JPanel {

    private static final long serialVersionUID = -7558362062012338814L;
    private Dimension elemSize = new Dimension(10, 10);
    private Clustering<Cluster> a;
    private Clustering<Cluster> b;
    protected int fontSize = 10;
    protected Font defaultFont;
    protected Dimension size = new Dimension(0, 0);
    protected BufferedImage bufferedImage;
    protected Graphics2D g;
    private int[][] confmat;
    private int min;
    private int max;
    protected ColorPalette colorScheme;

    public ConfusionTable() {
        initComponents();
    }

    private void initComponents() {
        defaultFont = new Font("verdana", Font.PLAIN, fontSize);
        colorScheme = new ColorScheme();
    }

    public void setClusterings(Clustering<Cluster> c1, Clustering<Cluster> c2) {
        this.a = c1;
        this.b = c2;

        confmat = countMutual(c1, c2);
        findMinMax(confmat);
        colorScheme.setRange(min, max);
        resetCache();
    }

    /**
     *
     * @param c1
     * @param c2
     * @return matrix with numbers of instances in same clusters
     */
    public int[][] countMutual(Clustering<Cluster> c1, Clustering<Cluster> c2) {
        int[][] conf = new int[c1.size()][c2.size()];
        //System.out.println("C1: " + c1.toString());
        //System.out.println("C2: " + c2.toString());
        Cluster<Instance> curr;
        for (int i = 0; i < c1.size(); i++) {
            curr = c1.get(i);
            for (Instance inst : curr) {
                for (int j = 0; j < c2.size(); j++) {
                    //System.out.println("looking for: " + inst.getIndex() + " found? " + clust.contains(inst.getIndex()));
                    if (c2.get(j).contains(inst.getIndex())) {
                        conf[i][j]++;
                        break;
                    }
                }
            }
        }
        //Dump.matrix(conf, "confmat", 2);
        return conf;
    }

    protected void updateSize(Dimension size) {
        elemSize = size;
        resetCache();
    }

    protected void createBufferedGraphics() {
        if (!hasData() || size.width == 0 || size.height == 0) {
            return;
        }
        bufferedImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        g = bufferedImage.createGraphics();
        this.setOpaque(false);
        // clear the panel
        g.setColor(getBackground());
        g.fillRect(0, 0, size.width, size.height);

        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        render(g);
        g.dispose();
    }

    public boolean hasData() {
        return (a != null && b != null);
    }

    public void render(Graphics2D g) {
        int x, y;
        int value;
        String s;
        g.setColor(Color.BLACK);
        g.setFont(defaultFont);
        FontRenderContext frc = g.getFontRenderContext();
        FontMetrics fm = g.getFontMetrics();
        int fh = fm.getHeight();
        double fw;

        for (int i = 0; i < a.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                value = confmat[i][j];
                //cnt = a.get(i).countMutualElements(b.get(j));
                //System.out.println("a-" + a.get(i).getName() + "-vs" + "-b-" + b.get(j).getName() + ": " + cnt);
                x = j * elemSize.width;
                y = i * elemSize.height;
                g.setColor(colorScheme.getColor(value));
                g.fillRect(x, y, elemSize.width, elemSize.height);
                s = String.valueOf(value);
                fw = (g.getFont().getStringBounds(s, frc).getWidth());
                g.setColor(colorScheme.complementary(g.getColor()));
                g.drawString(s, (int) (x - fw / 2 + elemSize.width / 2), y + elemSize.height / 2 + fh / 2);
                //draw rectangle around
                g.setColor(Color.BLACK);
                g.drawRect(x, y, elemSize.width - 1, elemSize.height - 1);
                //System.out.println("drawing rect: " + x + ", " + y + " w = " + elemSize.width + ", h= " + elemSize.height);
            }
        }

    }

    public void redraw() {
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        if (g2 == null) {
            return;
        }
        //buffered graphics is usually created before
        if (!hasData() && bufferedImage == null) {
            createBufferedGraphics();
        }
        if (bufferedImage != null) {
            g2.drawImage(bufferedImage,
                         0, 0,
                         size.width, size.height,
                         null);
        }
        g2.dispose();
    }

    protected void recalculate() {
        if (hasData()) {
            this.size.width = elemSize.width * a.size();
            this.size.height = elemSize.height * b.size();
            double fsize = elemSize.height * 0.3;
            defaultFont = defaultFont.deriveFont((float) fsize);
            //System.out.println("elem width = " + elemSize.width);
            //System.out.println("|a| = " + a.size());
            //System.out.println("matrix size: " + size.toString());
            setMinimumSize(this.size);
            setSize(this.size);
            setPreferredSize(size);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bufferedImage == null) {
            createBufferedGraphics();
        }
        //cached image
        g.drawImage(bufferedImage,
                    0, 0,
                    size.width, size.height,
                    null);
        g.dispose();
    }

    public void resetCache() {
        recalculate();
        createBufferedGraphics();
        repaint();
    }

    /**
     * Finds min and max value in confusion matrix
     *
     * @param confmat
     */
    private void findMinMax(int[][] confmat) {
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        for (int[] row : confmat) {
            for (int j = 0; j < row.length; j++) {
                if (row[j] < min) {
                    min = row[j];
                } else if (row[j] > max) {
                    max = row[j];
                }
            }
        }
    }

}
