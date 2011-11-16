package net.codjo.security.gui.login;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;
/**
 *
 */
class AnimatedSplash extends JWindow {
    private AnimatedPanel animatedPanel;


    AnimatedSplash(JFrame rootFrame, String messageBegin, ImageIcon icon) {
        super(rootFrame);
        animatedPanel = new AnimatedPanel(messageBegin, icon);
        getContentPane().setLayout(new BorderLayout());

        Dimension windowSize = new Dimension(icon.getIconWidth(), icon.getIconHeight() + 50);
        animatedPanel.setSize(windowSize);
        BevelBorder border = new BevelBorder(BevelBorder.RAISED);
        animatedPanel.setBorder(border);
        getContentPane().add(animatedPanel, BorderLayout.CENTER);
        setSize(windowSize);

        center(rootFrame, windowSize);
    }


    public void start() {
        setVisible(true);
        animatedPanel.start();
    }


    public void setMessage(String message) {
        animatedPanel.setMessage(message);
    }


    public void stop() {
        animatedPanel.stop();
        setVisible(false);
        dispose();
    }


    private void center(JFrame rootFrame, Dimension windowSize) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point point = new Point((int)((screenSize.getWidth() - windowSize.getWidth()) / 2),
                                (int)((screenSize.getHeight() - windowSize.getHeight()) / 2));
        if (rootFrame.getX() >= screenSize.getWidth()) {
            point.translate(screenSize.width, 0);
        }
        setLocation(point);
    }


    public class AnimatedPanel extends JPanel implements ActionListener {
        private static final int LOWER_BOUND = 10;
        private static final int UPPER_BOUND = 25;
        private float gradient;
        private String message;
        private BufferedImage convolvedImage;
        private BufferedImage originalImage;
        private Font font;
        private final Object hook = new Object();

        //    private Timer animation;
        //    private int delay = 300;
        private int way = 1;
        private int value = LOWER_BOUND;


        /**
         * Crée un panneau animé contenant l'image passée en paramètre. L'animation ne démarre que par un appel à
         * start().
         *
         * @param message Le message à afficher
         * @param icon    L'image à afficher et à animer
         */
        public AnimatedPanel(String message, ImageIcon icon) {
            this.message = message;
            this.font = getFont().deriveFont(14.0f);

            Image image = icon.getImage();
            originalImage =
                  new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                                    BufferedImage.TYPE_INT_ARGB);
            convolvedImage =
                  new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                                    BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = originalImage.createGraphics();
            graphics.drawImage(image, 0, 0, this);
            graphics.dispose();

            setBrightness(1.0f);
            setOpaque(false);
        }


        public void setMessage(String message) {
            this.message = message;
        }


        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (convolvedImage != null) {
                int width = getWidth();
                int height = getHeight();

                synchronized (hook) {
                    Graphics2D g2 = (Graphics2D)graphics;
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                        RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                    FontRenderContext context = g2.getFontRenderContext();
                    TextLayout layout = new TextLayout(message, font, context);
                    Rectangle2D bounds = layout.getBounds();

                    int intX = (width - convolvedImage.getWidth(null)) / 2;
                    int intY =
                          (int)(height
                                - (convolvedImage.getHeight(null) + bounds.getHeight()
                                   + layout.getAscent())) / 2;

                    g2.drawImage(convolvedImage, intX, intY, this);
                    g2.setColor(new Color(0, 0, 0, (int)(gradient * 255)));
                    layout.draw(g2, (float)(width - bounds.getWidth()) / 2,
                                (float)(intY + convolvedImage.getHeight(null)
                                        + bounds.getHeight() + layout.getAscent()));
                }
            }
        }


        /**
         * Modifie la luminosité de l'image.
         *
         * @param multiple Le taux de luminosité
         */
        private void setBrightness(float multiple) {
            float[] brightKernel = {multiple};
            RenderingHints hints =
                  new RenderingHints(RenderingHints.KEY_RENDERING,
                                     RenderingHints.VALUE_RENDER_QUALITY);
            BufferedImageOp bright =
                  new ConvolveOp(new Kernel(1, 1, brightKernel), ConvolveOp.EDGE_NO_OP,
                                 hints);
            bright.filter(originalImage, convolvedImage);
            repaint();
        }


        /**
         * Modifie le dégradé du texte.
         *
         * @param gradient Le coefficient de dégradé
         */
        private void setGradientFactor(float gradient) {
            this.gradient = gradient;
        }


        /**
         * Démarre l'animation du panneau.
         */
        public void start() {
//        animation = new Timer(delay, this);
//        animation.start();
            actionPerformed(null);
//        new Thread(this).start();
        }


        /**
         * Arrête l'animation.
         */
        public void stop() {
//        if (this.animation != null) {
//            animation.stop();
//        }
//        animation = null;
        }


        public void actionPerformed(ActionEvent event) {
            value += this.way;
            if (value > UPPER_BOUND) {
                value = UPPER_BOUND;
                this.way = -1;
            }
            else if (value < LOWER_BOUND) {
                value = LOWER_BOUND;
                this.way = 1;
            }

            synchronized (hook) {
                setBrightness((float)value / 10);
                setGradientFactor((float)value / UPPER_BOUND);
            }
        }
    }
}