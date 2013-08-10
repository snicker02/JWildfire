package org.jwildfire.create.tina.browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jwildfire.base.Prefs;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.io.Flam3Reader;
import org.jwildfire.create.tina.swing.TinaController;
import org.jwildfire.image.SimpleImage;
import org.jwildfire.swing.ErrorHandler;
import org.jwildfire.swing.ImagePanel;

public class FlameBrowserController {
  private final TinaController tinaController;
  private final ErrorHandler errorHandler;
  private final Prefs prefs;
  private final JPanel rootPanel;
  private final JTree flamesTree;
  private final JPanel imagesPnl;

  public FlameBrowserController(TinaController pTinaController, ErrorHandler pErrorHandler, Prefs pPrefs, JPanel pRootPanel, JTree pFlamesTree, JPanel pImagesPnl) {
    tinaController = pTinaController;
    errorHandler = pErrorHandler;
    prefs = pPrefs;
    rootPanel = pRootPanel;
    flamesTree = pFlamesTree;
    imagesPnl = pImagesPnl;
    enableControls();
  }

  public void enableControls() {

  }

  private boolean refreshing = false;

  private void refreshFlamesTree() {
    refreshing = true;
    try {
      String baseDrawer = prefs.getTinaFlamePath();
      flatFlameNodes.clear();
      if (baseDrawer != null && baseDrawer.length() > 0) {
        flatFlameNodes.scanFlames(baseDrawer);
      }
      flatFlameNodes.sortNodes();
      addFlatNodesToTree();
    }
    finally {
      refreshing = false;
    }
  }

  private void addFlatNodesToTree() {
    FlamesTreeNode root = new FlamesTreeNode("Flame library", true, null);
    final SimpleDateFormat monthDateFormat = new SimpleDateFormat("yyyy-MM");
    final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    List<Date> distinctMonths = flatFlameNodes.getDistinctMonths();
    for (Date month : distinctMonths) {
      List<FlameFlatNode> monthNodes = flatFlameNodes.getMonthNodes(month);
      FlamesTreeNode monthFolder = new FlamesTreeNode(monthDateFormat.format(month) + " (" + monthNodes.size() + ")", true, monthNodes);
      root.add(monthFolder);
      List<Date> distinctDays = flatFlameNodes.getDistinctDays(month);
      for (Date day : distinctDays) {
        List<FlameFlatNode> dayNodes = flatFlameNodes.getDayNodes(day);
        FlamesTreeNode dayFolder = new FlamesTreeNode(dayDateFormat.format(day) + " (" + dayNodes.size() + ")", true, dayNodes);
        monthFolder.add(dayFolder);
        for (FlameFlatNode flameNode : dayNodes) {
          FlamesTreeNode leafNode = new FlamesTreeNode(flameNode.getCaption(), true, dayNodes);
          dayFolder.add(leafNode);
        }
      }
    }

    flamesTree.setModel(new DefaultTreeModel(root));
  }

  public void init() {
    flamesTree.setRootVisible(false);
    addFlatNodesToTree();
  }

  private final FlameFlatNodes flatFlameNodes = new FlameFlatNodes();

  private FlamesTreeNode getSelNode() {
    FlamesTreeNode selNode = null;
    {
      TreePath selPath = flamesTree.getSelectionPath();
      if (selPath != null) {
        selNode = (FlamesTreeNode) selPath.getLastPathComponent();
      }
    }
    return selNode;
  }

  private JScrollPane imagesScrollPane;

  private void clearImages() {
    if (imagesScrollPane != null) {
      imagesPnl.remove(imagesScrollPane);
      imagesScrollPane = null;
    }
  }

  public void flamesTree_changed(TreeSelectionEvent e) {
    if (!refreshing) {
      enableControls();
      FlamesTreeNode selNode = getSelNode();
      if (selNode.isLeaf()) {
        int idx = 0;
        DefaultMutableTreeNode node = selNode;
        while (true) {
          node = node.getPreviousSibling();
          if (node == null) {
            break;
          }
          idx++;
        }
        selectCell(idx);
      }
      else {
        clearImages();
        if (selNode != null) {
          showImages(selNode.getFlames());
        }
        imagesPnl.validate();
      }
    }
  }

  private List<ImagePanel> pnlList = new ArrayList<ImagePanel>();
  private int selectedPnl = -1;
  private final Color deselectedCellColor = new Color(160, 160, 160);
  private final Color selectedCellColor = new Color(200, 0, 0);
  private final int borderSize = 3;
  private JPanel imgRootPanel;

  private void showImages(List<FlameFlatNode> pFlames) {
    cancelRenderThreads();
    final int IMG_WIDTH = 160;
    final int IMG_HEIGHT = 100;
    final int OUTER_BORDER = 20;
    final int INNER_BORDER = 10;
    final int LABEL_HEIGHT = 12;
    int maxPnlWidth = imagesPnl.getSize().width;
    int cols = calcCols(IMG_WIDTH, OUTER_BORDER, INNER_BORDER, maxPnlWidth);
    int rows = pFlames.size() / cols;
    if (rows * cols < pFlames.size()) {
      rows++;
    }

    int pnlWidth = 2 * OUTER_BORDER + (cols - 1) * INNER_BORDER + cols * IMG_WIDTH;
    int pnlHeight = 2 * OUTER_BORDER + (rows - 1) * INNER_BORDER + rows * (IMG_HEIGHT + LABEL_HEIGHT);

    imgRootPanel = new JPanel();
    imgRootPanel.setLayout(null);
    imgRootPanel.setSize(pnlWidth, pnlHeight);
    imgRootPanel.setPreferredSize(new Dimension(pnlWidth, pnlHeight));
    imgRootPanel.getActionMap().put("selectLeftCellAction", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectLeftCell();
      }
    });
    imgRootPanel.getActionMap().put("selectRightCellAction", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectRightCell();
      }
    });
    imgRootPanel.getActionMap().put("selectTopCellAction", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectUpperCell();
      }
    });
    imgRootPanel.getActionMap().put("selectBottomCellAction", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectLowerCell();
      }
    });

    int flameIdx = 0;
    pnlList = new ArrayList<ImagePanel>();
    selectedPnl = -1;
    int y = OUTER_BORDER;
    List<RenderJobInfo> jobInfoLst = new ArrayList<RenderJobInfo>();
    pnlList = new ArrayList<ImagePanel>();
    for (int r = 0; r < rows; r++) {
      int x = OUTER_BORDER;
      for (int c = 0; c < cols; c++) {
        if (flameIdx < pFlames.size()) {
          // image
          FlameFlatNode node = pFlames.get(flameIdx++);
          ImagePanel imgPanel;
          SimpleImage img = renderCache.getImage(node);
          if (img == null) {
            img = new SimpleImage(IMG_WIDTH, IMG_HEIGHT);
            img.fillBackground(0, 0, 0);
            imgPanel = new ImagePanel(img, 0, 0, img.getImageWidth());
            jobInfoLst.add(new RenderJobInfo(imgPanel, node, IMG_WIDTH, IMG_HEIGHT, x, y));
          }
          else {
            imgPanel = new ImagePanel(img, 0, 0, img.getImageWidth());
          }
          imgPanel.setImage(img);
          imgPanel.setLocation(x, y);
          imgPanel.setBorder(new LineBorder(deselectedCellColor, borderSize));
          pnlList.add(imgPanel);

          final String flameFilename = node.getFilename();
          final int pnlIdx = pnlList.size() - 1;

          imgPanel.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
              if (e.getClickCount() > 1 || e.getButton() != MouseEvent.BUTTON1) {
                List<Flame> flames = new Flam3Reader(prefs).readFlames(flameFilename);
                if (flames.size() > 0) {
                  for (Flame flame : flames) {
                    tinaController.importFlame(flame, true);
                  }
                  tinaController.getRootTabbedPane().setSelectedIndex(0);
                }
              }
            }

            @Override
            public void mousePressed(MouseEvent e) {
              selectCell(pnlIdx);
            }

          });
          imgRootPanel.add(imgPanel);
          // label with description 
          JLabel label = new JLabel();
          label.setText(node.getCaption());
          label.setSize(new Dimension(IMG_WIDTH, LABEL_HEIGHT));
          label.setPreferredSize(new Dimension(IMG_WIDTH, LABEL_HEIGHT));
          label.setLocation(new Point(x, y + IMG_HEIGHT));
          label.setFont(new Font("Dialog", Font.BOLD, 10));
          label.setHorizontalAlignment(SwingConstants.CENTER);
          label.setVerticalAlignment(SwingConstants.CENTER);
          imgRootPanel.add(label);
          //
          x += IMG_WIDTH + INNER_BORDER;
        }
        else {
          break;
        }
      }
      y += IMG_HEIGHT + INNER_BORDER + LABEL_HEIGHT;
    }
    selectedPnl = pnlList.size() > 0 ? 0 : -1;
    selectCell(selectedPnl);
    imagesScrollPane = new JScrollPane(imgRootPanel);
    imagesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    imagesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    imagesPnl.add(imagesScrollPane, BorderLayout.CENTER);

    if (jobInfoLst.size() > 0) {
      startRenderThread(jobInfoLst);
    }

  }

  protected void selectLowerCell() {
    // TODO Auto-generated method stub

  }

  protected void selectUpperCell() {
    // TODO Auto-generated method stub

  }

  protected void selectRightCell() {
    // TODO Auto-generated method stub

  }

  protected void selectLeftCell() {
    // TODO Auto-generated method stub
    System.out.println("left");
  }

  protected void selectCell(int pIndex) {
    for (int i = 0; i < pnlList.size(); i++) {
      ImagePanel pnl = pnlList.get(i);
      boolean sel = (i == pIndex);
      pnl.setBorder(new LineBorder(sel ? selectedCellColor : deselectedCellColor, borderSize));
    }
    imgRootPanel.invalidate();
    imgRootPanel.validate();
    selectedPnl = pIndex;
  }

  private void startRenderThread(List<RenderJobInfo> jobInfoLst) {
    CacheRendererThread renderThread = new CacheRendererThread(prefs, renderCache, jobInfoLst);
    new Thread(renderThread).start();
    renderThreads.add(renderThread);
  }

  private void cancelRenderThreads() {
    final int MAX_RUNNING_THREADS = 4;
    for (CacheRendererThread thread : renderThreads) {
      thread.signalCancel();
    }
    while (renderThreads.size() > MAX_RUNNING_THREADS) {
      CacheRendererThread thread = renderThreads.get(0);
      while (!thread.isDone()) {
        try {
          Thread.sleep(3);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      renderThreads.remove(thread);
    }
  }

  private List<CacheRendererThread> renderThreads = new ArrayList<CacheRendererThread>();

  private int calcCols(int pImgWidth, int pOuterBorder, int pInnerBorder, int pMaxPnlWidth) {
    int cols = 0;
    while (true) {
      cols++;
      int width = 2 * pOuterBorder + (cols - 1) * pInnerBorder + cols * pImgWidth;
      if (width > pMaxPnlWidth) {
        cols--;
        if (cols < 1) {
          cols = 1;
        }
        break;
      }
    }
    return cols;
  }

  private final RenderCache renderCache = new RenderCache();

  public void refreshBtn_clicked() {
    refreshFlamesTree();
  }

}
