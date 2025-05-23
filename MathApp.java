import java.io.*; 
import java.awt.*;
import java.awt.Graphics;
import java.awt.Color; 
import java.awt.BorderLayout; 
import java.awt.GridLayout; 
import java.awt.Insets; 
import java.awt.Dimension; 
import java.awt.event.ActionEvent; 
import java.awt.event.*;
import java.util.HashMap;
import java.util.Vector; 

import javax.swing.*;
import javax.swing.JFrame; 
import javax.swing.text.*;
import javax.swing.event.*;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

/* K. Lano 2010-2025
   
  Adapted from Oracle example of JTextPane

 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
   Package: GUI
*/ 

public class MathApp extends JFrame implements DocumentListener, ActionListener
{  JPanel buttonsPanel; 
   JTextPane textPane;
   DefaultStyledDocument doc;
   JTextArea messageArea;
   String newline = "\n";
   HashMap actions;

   JEditorPane helpPane = null; 
   JEditorPane umlPane = null; 

   String systemName = "app"; 
   private JLabel thisLabel;
   String insertedText = ""; 
   boolean inserting = false; 

   SimpleAttributeSet[] attrs; 
   SimpleAttributeSet currentAttrs; 

   Vector entities = new Vector(); // holds the UML spec
   Vector types = new Vector();
   String internalModel = "";  

    java.util.Map charMap = new java.util.HashMap(); 
    java.util.Set encodedChars = new java.util.HashSet(); 

    char mSigma = '\u2211'; // �
    char mPi = '\u220F'; // �
    char mSqrt = '\u221A'; // �
    char mInfinity = '\u221E'; // � �
    char mIntegral = '\u222B';  // �
    char mDifferential = '\u2032'; // �
    char mPartialDiff = '\u2202'; // �
    char mExists = '\u018E'; // �
    char mForall = '\u2200'; // �
    char mIn = '\u2208';  // �
    char mNotIn = '\u2209'; // �
    char mNatural = '\u2115'; // �
    char mIntegers = '\u2124'; // �
    char mReals = '\u211D'; // �

    char emptySet = '�';

    public MathApp() 
    { super("MathOCL Editor"); 

      addWindowListener(new WindowAdapter() 
      { public void windowClosing(WindowEvent e) 
        { System.exit(0); }
      });
    
      charMap.put(mSigma, '�'); 
      charMap.put(mPi, '�'); 
      charMap.put(mSqrt, '�'); 
      charMap.put(mInfinity, '�'); 
      charMap.put(mIntegral, '�'); 
      charMap.put(mDifferential, '�'); 
      charMap.put(mPartialDiff, '�'); 
      charMap.put(mExists, '�'); 
      charMap.put(mForall, '�'); 
      charMap.put(mIn, '�'); 
      charMap.put(mNotIn, '�'); 
      charMap.put(mNatural, '�'); 
      charMap.put(mIntegers, '�'); 
      charMap.put(mReals, '�'); 

        // types = new Vector(); 

        textPane = new JTextPane();
        textPane.setCaretPosition(0);
        textPane.setMargin(new Insets(5,5,5,5));
        StyledDocument styledDoc = textPane.getStyledDocument();
        if (styledDoc instanceof DefaultStyledDocument) 
        { doc = (DefaultStyledDocument) styledDoc;
          doc.addDocumentListener(this); 
        } 
	   else 
	   {
          System.err.println("!! Error: invalid document");
        }

      buttonsPanel = new JPanel();
      buttonsPanel.setLayout(new GridLayout(2,16)); 
 
      JButton bsum = new JButton("" + mSigma); 
      bsum.addActionListener(this); 
      bsum.setToolTipText(
        "Sum of expressions: " + mSigma + '\u2096' + '\u208C' + '\u2092' + '\u207F' + " expr");
      JButton bprd = new JButton("" + mPi); 
      bprd.addActionListener(this); 
      bprd.setToolTipText(
        "Product of expressions: " + mPi + '\u2096' + '\u208C' + '\u2092' + '\u207F' + " expr");
      JButton bsqrt = new JButton("" + mSqrt); 
      bsqrt.setToolTipText("Square root"); 
      bsqrt.addActionListener(this); 
      JButton binfty = new JButton("" + mInfinity); 
      binfty.setToolTipText("Positive infinity: Math_PINFINITY"); 
      binfty.addActionListener(this); 
      JButton bintegral = new JButton("" + mIntegral); 
      bintegral.setToolTipText("Integral, definite & indefinite: " + mIntegral + "expr dx"); 
      bintegral.addActionListener(this); 
      
      JButton bdiff = new JButton("" + mDifferential); 
      bdiff.addActionListener(this); 
      bdiff.setToolTipText(
        "Differential wrt x: f" + mDifferential + " is df/dx");

      JButton bpdiff = new JButton("" + mPartialDiff); 
      bpdiff.addActionListener(this); 
      bpdiff.setToolTipText(
        "Partial differential wrt variable: " + mPartialDiff + '\u209C' + " f is " + mPartialDiff + "f/" + mPartialDiff + "t");
      
      JButton bexists = new JButton("" + mExists); 
      bexists.addActionListener(this); 
      bexists.setToolTipText("Existential quantifier: " + 
        mExists + " var : type \u2219 expr");

      JButton bforall = new JButton("" + mForall); 
      bforall.addActionListener(this); 
      bforall.setToolTipText("Universal quantifier: " + 
        mForall + " var : type \u2219 expr");
      
      JButton bin = new JButton("" + mIn); 
      bin.addActionListener(this); 
      bin.setToolTipText(
        "Set membership: x " + mIn + " s");

      JButton bnotin = new JButton("" + mNotIn); 
      bnotin.addActionListener(this); 
      bnotin.setToolTipText(
        "Set exclusion: x " + mNotIn + " s");

      JButton bempty = new JButton("" + emptySet); 
      bempty.addActionListener(this); 
      bempty.setToolTipText(
        "Empty set: Set{}");

      JButton bnatural = new JButton("" + mNatural); 
      bnatural.addActionListener(this); 
      bnatural.setToolTipText(
        "Type of non-negative integers 0, 1, 2, etc");

      JButton bintegers = new JButton("" + mIntegers); 
      bintegers.addActionListener(this); 
      bintegers.setToolTipText(
        "Type of all integers 0, 1, -1, 2, -2, etc");

      JButton breals = new JButton("" + mReals); 
      breals.addActionListener(this); 
      breals.setToolTipText(
        "Type of all real numbers");

      JButton bspot = new JButton("\u2219"); // � 
      bspot.addActionListener(this); 
      bspot.setToolTipText(
        "Used for " + mForall + " and " + mExists);

      JButton bsim = new JButton("\u2248"); // ~
      bsim.addActionListener(this); 
      JButton btends = new JButton("\u2192"); // � 
      btends.addActionListener(this);
      btends.setToolTipText(
        "Used for lim_{x " + '\u2192' + " v} expr");

      charMap.put('\u2219', '�'); // such that 
      charMap.put('\u2248', '~'); // almost equal
      charMap.put('\u2192', '�'); // tends to
      
      buttonsPanel.add(bexists);
      buttonsPanel.add(bforall);
      buttonsPanel.add(bin);
      buttonsPanel.add(bnotin);
      buttonsPanel.add(bempty);

      buttonsPanel.add(bspot);
      buttonsPanel.add(bsim);
      buttonsPanel.add(btends);
      
      buttonsPanel.add(bsum);
      buttonsPanel.add(bprd);
      buttonsPanel.add(bsqrt);
      buttonsPanel.add(bintegral);
      buttonsPanel.add(bdiff);
      buttonsPanel.add(bpdiff);
      buttonsPanel.add(binfty);

      buttonsPanel.add(bnatural);
      buttonsPanel.add(bintegers);
      buttonsPanel.add(breals);

      JButton alpha = new JButton("\u03B1"); 
      alpha.addActionListener(this); 
      alpha.setToolTipText("alpha"); 
      JButton beta = new JButton("\u03B2"); // �
      beta.addActionListener(this);
      beta.setToolTipText("beta"); 
       
      JButton gamma = new JButton("\u03B3"); 
      gamma.addActionListener(this); 
      gamma.setToolTipText("gamma"); 

      JButton delta = new JButton("\u03B4"); 
      delta.addActionListener(this); 
      delta.setToolTipText("delta"); 
      
      JButton epsilon = new JButton("\u03B5"); 
      epsilon.addActionListener(this);
      epsilon.setToolTipText("epsilon"); 
       
      JButton zeta = new JButton("\u03B6"); 
      zeta.addActionListener(this); 
      zeta.setToolTipText("zeta"); 
      
      JButton theta = new JButton("\u03B8"); 
      theta.addActionListener(this);
      theta.setToolTipText("theta"); 
       
      JButton lambda = new JButton("\u03BB"); 
      lambda.addActionListener(this);
      lambda.setToolTipText("lambda"); 
       
      JButton mu = new JButton("\u03BC"); // �
      mu.addActionListener(this); 
      mu.setToolTipText("mu"); 
      
      JButton nu = new JButton("\u03BD"); 
      nu.addActionListener(this);
      nu.setToolTipText("nu"); 
       
      JButton pi = new JButton("\u03C0"); 
      pi.addActionListener(this);
      pi.setToolTipText("pi -- the value 3.141592653589"); 
       
      JButton rho = new JButton("\u03C1"); 
      rho.addActionListener(this); 
      rho.setToolTipText("rho"); 
      
      JButton sigma = new JButton("\u03C3"); 
      sigma.addActionListener(this);
      sigma.setToolTipText("sigma"); 
       
      JButton tau = new JButton("\u03C4"); 
      tau.addActionListener(this);
      tau.setToolTipText("tau"); 
       
      JButton chi = new JButton("\u03C7"); 
      chi.addActionListener(this);
      chi.setToolTipText("chi"); 
       
      JButton omega = new JButton("\u03C9"); 
      omega.addActionListener(this); 
      omega.setToolTipText("omega"); 
      
      charMap.put('\u03B1', "g{a}"); 
      charMap.put('\u03B2', "g{b}"); // �
      charMap.put('\u03B3', "g{g}"); 
      charMap.put('\u03B4', "g{d}"); 
      charMap.put('\u03B5', "g{e}"); 
      charMap.put('\u03B6', "g{z}"); 
      charMap.put('\u03B8', "g{f}"); 
      charMap.put('\u03BB', "g{l}"); 
      charMap.put('\u03BC', "g{m}"); // �
      charMap.put('\u03BD', "g{n}"); 
      charMap.put('\u03C0', "g{p}"); 
      charMap.put('\u03C1', "g{r}");      
      charMap.put('\u03C3', "g{s}"); 
      charMap.put('\u03C4', "g{t}"); 
      charMap.put('\u03C7', "g{c}"); 
      charMap.put('\u03C9', "g{o}"); 
      encodedChars.addAll(charMap.keySet()); 

      buttonsPanel.add(alpha);
      buttonsPanel.add(beta);
      buttonsPanel.add(gamma);
      buttonsPanel.add(delta);

      buttonsPanel.add(epsilon);
      buttonsPanel.add(zeta);
      buttonsPanel.add(theta);
      buttonsPanel.add(lambda);
      buttonsPanel.add(mu);
      buttonsPanel.add(nu);
      buttonsPanel.add(pi);
      buttonsPanel.add(rho);
      buttonsPanel.add(sigma);
      buttonsPanel.add(tau);
      buttonsPanel.add(chi);
      buttonsPanel.add(omega);

      attrs = initAttributes(6);
      currentAttrs = attrs[0]; 

      try {
            doc.insertString(0, "specification S \n", attrs[0]); 
          }
          catch (BadLocationException ble) {
            System.err.println("!! Couldn't insert code text.");
        } 

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(650, 350));

        messageArea = new JTextArea(30, 90);
        messageArea.setFont(new Font("SansSerif", Font.BOLD, 16)); 
        messageArea.setEditable(true);
        JScrollPane scrollPaneForLog = new JScrollPane(messageArea);

        JSplitPane splitPane = new JSplitPane(
                       JSplitPane.VERTICAL_SPLIT,
                       scrollPane, scrollPaneForLog);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(300); 

        // JPanel statusPane = new JPanel(new GridLayout(2, 1));

        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.NORTH);
        thisLabel = 
          new JLabel("Type within the framed area.");
        getContentPane().add(thisLabel, BorderLayout.SOUTH); 

        actions = createActionTable(textPane);

        JMenu fileMenu = createFileMenu(); 
        JMenu editMenu = createEditMenu(); 
        // JMenu absMenu = createAbstractionMenu(); 
        JMenu transMenu = createTranslationMenu();
        JMenu styleMenu = createStyleMenu();
        JMenu analysisMenu = createAnalysisMenu();
        JMenu helpMenu = createHelpMenu(); 
        
        JMenuBar mb = new JMenuBar();

        mb.add(fileMenu); 
        mb.add(editMenu); 
        // mb.add(absMenu); 
        mb.add(styleMenu);
        mb.add(analysisMenu);
        mb.add(transMenu);
        mb.add(helpMenu); 
        
        setJMenuBar(mb);

        textPane.setCaretPosition(0);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ee) 
    { String cmd = ee.getActionCommand(); 
      try { StyledDocument doc = textPane.getStyledDocument();
            int pos = textPane.getCaretPosition();
            doc.insertString(pos, cmd, currentAttrs); 
                     // , attrs[0]);
          }
          catch (BadLocationException ble) {
            System.err.println("!! Couldn't insert text.");
        } 
    } 

    // public void paint(Graphics g)
    // { setVisible(true); } 

    private javax.swing.Action getActionByName(String name) 
    { return (javax.swing.Action) actions.get(name); }

    protected JMenu createStyleMenu() {
        JMenu menu = new JMenu("Style");

        PositionTextAction subscriptAction = 
           new PositionTextAction("Subscript", this); 
        subscriptAction.setAttributes(textPane, attrs[4]); 
        menu.add(subscriptAction);
        PositionTextAction superscriptAction = 
           new PositionTextAction("Superscript", this); 
        superscriptAction.setAttributes(textPane, attrs[5]); 
        menu.add(superscriptAction);
        PositionTextAction normalAction = 
           new PositionTextAction("Normal", this);
        normalAction.setAttributes(textPane, attrs[0]); 
        menu.add(normalAction); 

        return menu;
    }

    protected JMenu createEditMenu() 
    { JMenu menu = new JMenu("Edit");

      javax.swing.Action cAction = getActionByName(DefaultEditorKit.cutAction);
        // cAction.setValue(Action.NAME,"Cut");  
      JMenuItem cutMI = menu.add(cAction);
      cutMI.setLabel("Cut"); 
      
      javax.swing.Action cpyAction = getActionByName(DefaultEditorKit.copyAction);
      JMenuItem copyMI = menu.add(cpyAction); 
      copyMI.setLabel("Copy"); 
      
      javax.swing.Action pteAction = getActionByName(DefaultEditorKit.pasteAction);
      JMenuItem pasteMI = menu.add(pteAction); 
      pasteMI.setLabel("Paste"); 

      return menu;
    }

    protected JMenu createAnalysisMenu() 
    { JMenu menu = new JMenu("Analysis");

      javax.swing.Action checkAction = new CheckAction(); 
        // checkAction.setMnemonic(KeyEvent.VK_K);
      menu.setToolTipText(
                      "Check & simplify the specification");
      menu.add(checkAction); 

      javax.swing.Action analyseAction = new AnalyseAction(); 
      menu.add(analyseAction); 


      return menu; 
   } 

    protected JMenu createTranslationMenu() 
    { JMenu menu = new JMenu("Translate");

      // Also, translate to OCL, translate to Matlab

      menu.setToolTipText(
              "Translate to OCL, Mamba, code");

      javax.swing.Action km3Action = new KM3Action(); 
      menu.add(km3Action); 

      // javax.swing.Action matlabAction = new MatlabAction(); 
      // menu.add(matlabAction); 

      javax.swing.Action mambaAction = new MambaAction(); 
      menu.add(mambaAction); 

      menu.addSeparator(); 

      javax.swing.Action toJavaAction = new Translate2JavaAction(); 
        // checkAction.setMnemonic(KeyEvent.VK_K);
      menu.add(toJavaAction); 

      javax.swing.Action toCSAction = new Translate2CSAction(); 
        // checkAction.setMnemonic(KeyEvent.VK_K);
      menu.add(toCSAction); 

      javax.swing.Action toCPPAction = new Translate2CPPAction(); 
        // checkAction.setMnemonic(KeyEvent.VK_K);
      menu.add(toCPPAction); 

      javax.swing.Action toPythonAction = 
                     new Translate2PythonAction(); 
        // checkAction.setMnemonic(KeyEvent.VK_K);
      menu.add(toPythonAction); 

      return menu; 
   } 

    protected JMenu createHelpMenu() 
    { JMenu menu = new JMenu("Help");

      javax.swing.Action helpNotationAction = new HelpNotationAction(); 
      menu.add(helpNotationAction); 

      javax.swing.Action helpProcessAction = new HelpProcessAction(); 
      menu.add(helpProcessAction); 

      javax.swing.Action helpOCLAction = new HelpOCLAction(); 
      menu.add(helpOCLAction); 

      return menu; 
   } 

    public void changedUpdate(DocumentEvent e)
    { int offset = e.getOffset(); 
	 int pos = textPane.getCaretPosition();
      // System.out.println(">> Update event at: " + offset + " " + pos); 
    }

	public void removeUpdate(DocumentEvent e)
	{ int offset = e.getOffset(); 
	  int pos = textPane.getCaretPosition();
       // System.out.println(">> Remove event at: " + offset + " " + pos); 
	}

	public void insertUpdate(DocumentEvent e)
	{ int offset = e.getOffset(); 
	  int pos = textPane.getCaretPosition();
       try {
          // System.out.println(">> Insert event " + e + " at: " + offset + " " + pos + " " + textPane.getText(pos,1));
          String chr = "" + textPane.getText(pos,1); 
          if ("~".equals(chr))
          { thisLabel.setText("~ Declares a random variable: Define X ~ Dist sets X as random variable from Dist"); }
          else if ("[".equals(chr))
          { thisLabel.setText("E[expr] computes expectation (mean) of expression expr involving random variables"); }
          else if ("{".equals(chr) || "}".equals(chr))
          { thisLabel.setText("{ var : type | cond } is subset of type satisfying cond. { var : type | cond " + '\u2219' + " expr } is collection of expr for these elements"); }
          else if (chr.length() > 0 && 
                   '\u222B' == chr.charAt(0))
          { thisLabel.setText("Integration with/without bounds, eg: \u222B f(x) dx  for indefinite integral."); }  
          else if (chr.length() > 0 &&
                   '\u2032' == chr.charAt(0))
          { thisLabel.setText("Differential wrt x: f\u2032 is df/dx"); } 
          else if (chr.length() > 0 &&
                   '\u2202' == chr.charAt(0))
          { thisLabel.setText("Partial differential wrt subscript: \u2202\u209C f is partial diff of f wrt t"); } 
          else if (chr.length() > 0 && 
                   Character.isLetter(chr.charAt(0)))
          { inserting = true; 
            insertedText = insertedText + chr.charAt(0); 
            if ("Define".equals(insertedText))
            { thisLabel.setText("Define variable: Define v, Define v = expr, Define v : type, Define v : type = expr, Define v = instruction, Define v ~ distribution"); }
            else if ("Solve".equals(insertedText))
            { thisLabel.setText("Solve single quadratic or differential equations, and multiple linear equations: Solve eqns for vars"); }
            else if ("Prove".equals(insertedText))
            { thisLabel.setText("Prove/Claim expr1 follows from expr2: Prove expr1 if expr2"); }
            else if ("Constraint".equals(insertedText))
            { thisLabel.setText("Constraint on a variable: Constraint on var | expr"); }
            else if ("Simplify".equals(insertedText))
            { thisLabel.setText("Simplify an expression: Simplify expr"); }
            else if ("Factor".equals(insertedText))
            { thisLabel.setText("Instruction to Factor: Factor expr by var"); }
            else if ("Cancel".equals(insertedText))
            { thisLabel.setText("Instruction to Cancel: Cancel var in expr"); }
            else if ("Substitute".equals(insertedText))
            { thisLabel.setText("Instruction to Substitute: Substitute var in expr"); }
            else if ("Group".equals(insertedText))
            { thisLabel.setText("Group expr by var: group together terms with same var power"); }
            else if ("Expand".equals(insertedText))
            { thisLabel.setText("Instruction to Expand: Expand expr to n terms"); }
            else if ("Express".equals(insertedText))
            { thisLabel.setText("Instruction to Express: Express expr as polynomial in var"); }
            else if ("Theorem".equals(insertedText))
            { thisLabel.setText("Theorem expr1 when expr2: assert that expr1 follows from expr2"); }
            else if ("Rewrite".equals(insertedText))
            { thisLabel.setText("Rewrite expr1 to expr2: assert that expr1 can be replaced by expr2"); }
            else if ("Bernoulli".equals(insertedText))
            { thisLabel.setText("Bernoulli distribution Bernoulli(p) for probability p of success"); }
            else if ("Binom".equals(insertedText))
            { thisLabel.setText("Binomial distribution Binom(n,p) for n trials, probability p of success"); }
            else if ("Poisson".equals(insertedText))
            { thisLabel.setText("Poisson distribution Poisson(p)"); }
            else if ("LogNorm".equals(insertedText))
            { thisLabel.setText("Log-normal distribution LogNorm(mu,var) based on N(mu,var)"); }
            else if ("N(".equals(insertedText))
            { thisLabel.setText("Normal distribution N(mu,var)"); }
            else if ("U(".equals(insertedText))
            { thisLabel.setText("Uniform distribution U() or U(lower,upper)"); }
            else if ("lim".equals(insertedText))
            { thisLabel.setText("Limit: lim_{x � v} expr"); }
          } 
          else  
          { thisLabel.setText(" "); 
            inserting = false; 
            insertedText = ""; 
          }   
        } catch (Exception _e) { } 
     } 

    private HashMap createActionTable(JTextComponent textComponent) 
    { HashMap actions = new HashMap();
      javax.swing.Action[] actionsArray = textComponent.getActions();
      for (int i = 0; i < actionsArray.length; i++) 
      { javax.swing.Action a = actionsArray[i];
        actions.put(a.getValue(javax.swing.Action.NAME), a);
      }
      return actions;
    }

    protected SimpleAttributeSet[] initAttributes(int len) 
    {  // len >= 6

        SimpleAttributeSet[] attrs = new SimpleAttributeSet[len];

        attrs[0] = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs[0], "SansSerif");
        StyleConstants.setFontSize(attrs[0], 16);
        StyleConstants.setSubscript(attrs[0], false);
        StyleConstants.setSuperscript(attrs[0], false);

        attrs[1] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setBold(attrs[1], true);

        attrs[2] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setItalic(attrs[2], true);

        attrs[3] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setFontSize(attrs[3], 20);

        attrs[4] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setSubscript(attrs[4], true);
        StyleConstants.setFontSize(attrs[4], 12);

        attrs[5] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setSuperscript(attrs[5], true);
        StyleConstants.setFontSize(attrs[5], 12);

        return attrs;
    }

    protected JMenu createFileMenu() 
    { JMenu menu = new JMenu("File");

      Action loadAction = new LoadAction(); 
        // checkAction.setMnemonic(KeyEvent.VK_K);
      JMenuItem loadMI = menu.add(loadAction); 
      loadMI.setToolTipText("Load from Test.xml, data.ser"); 

      Action saveAction = new SaveAction(); 
        // checkAction.setMnemonic(KeyEvent.VK_K);
      JMenuItem saveMI = menu.add(saveAction);
      saveMI.setToolTipText("Save in Test.xml, data.ser"); 
 
      return menu; 
   } 

  public void loadKM3FromFile(File file)
  { 
    BufferedReader br = null;
    Vector res = new Vector();
    String s;
    boolean eof = false;
    
    try
    { br = new BufferedReader(new FileReader(file)); }
    catch (FileNotFoundException e)
    { System.out.println("!! File not found: " + file.getName());
      return; 
    }

    String xmlstring = ""; 
    int linecount = 0; 

    while (!eof)
    { try 
      { s = br.readLine(); }
      catch (IOException e)
      { System.out.println("!! Reading " + file.getName() + " failed.");
        return; 
      }

      if (s == null) 
      { eof = true; 
        break; 
      }
      else if (s.startsWith("import ")) 
      { System.out.println(">> Import directive: " + s); } 
      else if (s.startsWith("//"))
      { System.out.println(">> Comment: " + s); }
      else  
      { xmlstring = xmlstring + s + " "; } 
      linecount++; 
    }

    System.out.println(">>> " + linecount + " lines in KM3 file"); 
    loadKM3FromText(xmlstring); 
  }

  private void loadKM3FromText(String xmlstring)
  { 
    Vector oldentities = new Vector(); 
    oldentities.addAll(entities); 
	
    Vector oldtypes = new Vector(); 
    oldtypes.addAll(types); 
	
    Vector pregens = new Vector(); 
    Vector preassocs = new Vector(); 
    Vector pnames = new Vector(); 

    Compiler2 comp = new Compiler2();  
    comp.nospacelexicalanalysis(xmlstring);

    Vector xentities = new Vector(); 
    Vector xtypes = new Vector(); 
    comp.identifyKM3classifiers(xentities,xtypes); 

    System.out.println(">>> Identified classes: " + xentities); 
    System.out.println(">>> Identified types: " + xtypes); 

    // As bases for parsing, also include oldentities and 
    // oldtypes which are not in xentities, xtypes

    for (int i = 0; i < oldentities.size(); i++) 
    { Entity oldent = (Entity) oldentities.get(i); 
      String oldname = oldent.getName(); 
      Entity ex = 
        (Entity) ModelElement.lookupByName(oldname,xentities); 
      if (ex == null)
      { xentities.add(oldent); } 
    } 

    for (int i = 0; i < oldtypes.size(); i++) 
    { Type oldtyp = (Type) oldtypes.get(i); 
      // String oldname = oldtyp.getName(); 
      // Type tx = 
      //   (Type) ModelElement.lookupByName(oldname,xtypes); 
      // if (tx == null)
      { xtypes.add(oldtyp); } 
    } 
 
    Vector items = comp.parseKM3(xentities,xtypes,
                                 pregens,preassocs,pnames); 
    System.out.println(">>> Packages " + pnames + " in KM3 file"); 
    /* if (pnames.size() > 0) 
    { setSystemName((String) pnames.get(0)); }
	
    Vector passocs = new Vector(); 
    passocs.addAll(preassocs); 

    for (int aa = 0; aa < preassocs.size(); aa++) 
    { PreAssociation a1 = (PreAssociation) preassocs.get(aa); 
      for (int bb = aa+1; bb < preassocs.size(); bb++) 
      { PreAssociation b1 = (PreAssociation) preassocs.get(bb); 
        if (a1.isDualTo(b1)) 
        { PreAssociation c1 = a1.combineWith(b1); 
          passocs.remove(a1); 
          passocs.remove(b1); 
          passocs.add(c1); 
        } 
      } 
    } 

    int delta = 180; // visual displacement 
    int ecount = oldentities.size() + 1; */ 

    Vector newentities = new Vector(); 
    newentities.addAll(xentities); 
    newentities.removeAll(oldentities); 

    System.out.println(">>> New entities: " + newentities); 
	
   
    for (int i = 0; i < newentities.size(); i++) 
    { Entity enode = (Entity) newentities.get(i);
      if (enode.isGenericParameter()) 
      { continue; } 

     /* int xval = 200 + (ecount/5)*delta + ((ecount % 5)*delta)/5; 
      int yval = 150 + (ecount % 5)*delta; 
 
      String ex = enode.getTaggedValue("x"); 
      if (ex != null) 
      { xval = Integer.parseInt(ex); } 

      String ey = enode.getTaggedValue("y"); 
      if (ey != null) 
      { yval = Integer.parseInt(ey); } */ 

      entities.add(enode); 
      // addEntity(enode, xval, yval); 
      // addOperationActivities(enode); 
         // But not for existing entities  
      // ecount++; 
    } 

    Vector newtypes = new Vector(); 
    newtypes.addAll(xtypes); 
    newtypes.removeAll(oldtypes); 

  /* 
    for (int j = 0; j < newtypes.size(); j++) 
    { Type tt = (Type) newtypes.get(j); 
      // if (tt.isEnumeration())
      { RectData rd = new RectData(120*j,20,getForeground(),
                                 componentMode,
                                 rectcount);
        rectcount++;
        rd.setLabel(tt.getName());
        Type existingtype = 
          (Type) ModelElement.lookupByName(
                                 tt.getName(),types); 
        if (existingtype != null) 
        { existingtype.mergeTypes(tt); }
        else 
        { existingtype = tt;
          types.add(tt);
        } 
        rd.setModelElement(existingtype); 
        visuals.add(rd); 
        System.out.println(">>> Added type " + existingtype); 
      } 
    } 

    for (int p = 0; p < pregens.size(); p++) 
    { PreGeneralisation pg = (PreGeneralisation) pregens.get(p); 
      String e1n = pg.e1name;   // subclass
      String e2n = pg.e2name;   // superclass 
      Entity subc = 
        (Entity) ModelElement.lookupByName(e1n,entities);
      Entity supc = 
        (Entity) ModelElement.lookupByName(e2n,entities);

      if (subc == null) 
      { System.err.println("!!! ERROR: no subclass entity " + pg.e1name); } 
      if (supc == null) 
      { System.err.println("!!! ERROR: no superclass entity " +  pg.e2name); } 

      if (subc != null && supc != null) 
      { Generalisation g = new Generalisation(supc,subc);
        addInheritance(g,supc,subc); 
        supc.setAbstract(true); 
      } 
    } 

    for (int q = 0; q < passocs.size(); q++) 
    { PreAssociation pa = (PreAssociation) passocs.get(q);  
      Entity e1 =
        (Entity) ModelElement.lookupByName(pa.e1name,entities);
      Entity e2 =
        (Entity) ModelElement.lookupByName(pa.e2name,entities);

      if (e1 == null) 
      { System.err.println("!!! ERROR: no entity " + pa.e1name); } 
      if (e2 == null) 
      { System.err.println("!!! ERROR: no entity for " + pa.e1name + "." + pa.role2 + " : " + pa.e2name); } 

      
      if (e1 != null && e2 != null) 
      { RectData rd1 = (RectData) getVisualOf(e1); 
        RectData rd2 = (RectData) getVisualOf(e2);
        Vector linecoords = lineCoordinates(rd1, rd2, e1, e2); 

        int xs = ((Integer) linecoords.get(0)).intValue();
        int ys = ((Integer) linecoords.get(1)).intValue(); 
        int xe = ((Integer) linecoords.get(2)).intValue(); 
        int ye = ((Integer) linecoords.get(3)).intValue();   

        if (rd1 == rd2)
        { xs = rd2.sourcex + 10; 
          xe = rd2.sourcex + rd2.width - 10;
          ys = rd2.sourcey + 10;  
          ye = rd2.sourcey + rd2.height - 10; 
        }

        reconstructAssociation(pa.e1name,pa.e2name,xs,ys,
                    xe,ye,pa.card1,pa.card2,
                    pa.role2, pa.role1, pa.stereotypes, 
                    new Vector());
      } 
    } 

    for (int h = 0; h < items.size(); h++) 
    { Object hx = items.get(h); 
      if (hx instanceof UseCase) 
      { addGeneralUseCase((UseCase) hx); } 
    } 

    repaint(); */  
  }

  class SaveAction extends AbstractAction
  { public SaveAction()
    { super("Save"); }

    public void actionPerformed(ActionEvent e)
    { int pos = doc.getLength(); 
      int pos2 = textPane.getCaretPosition();
      if (pos2 > pos) 
      { pos = pos2; } 
      if (pos == 0) 
      { return; }

      String styles = ""; 
          
      for (int i = 0; i < pos; i++)
      { try 
        { textPane.setCaretPosition(i);
          AttributeSet st = textPane.getCharacterAttributes();

          if (st.isEqual(attrs[0]))
          { styles = styles + "p"; } 
          else if (st.isEqual(attrs[4]))
          { styles = styles + "_"; } 
          else if (st.isEqual(attrs[5]))
          { styles = styles + "^"; } 
        } catch (Exception ex) { }  
      }  
     

      ObjectOutputStream ostream = null;
      try 
      { int len = doc.getLength();
      
        XMLEncoder xe = new XMLEncoder(
                          new BufferedOutputStream(
                              new FileOutputStream("Test.xml")));
        // System.out.println(doc.getText(0,len)); 
        xe.writeObject(doc.getText(0,len));
        xe.close();

        ostream = new ObjectOutputStream(new FileOutputStream("data.ser"));
        ostream.writeObject(styles); 
        ostream.close(); 
      }
      catch(Exception _e) { _e.printStackTrace();
                            return; } 
      thisLabel.setText("Specification saved in Test.xml, data.ser");
    } 
  } 

  class LoadAction extends AbstractAction
  { public LoadAction()
    { super("Load from file"); }

    public void actionPerformed(ActionEvent e)
    { ObjectInputStream istream = null;
      try
      { XMLDecoder xd = new XMLDecoder(
                          new BufferedInputStream(
                              new FileInputStream("Test.xml")));
        Object result = xd.readObject();
        xd.close();
       
        
        FileInputStream br = new FileInputStream("./data.ser");
        if (br == null) { return; }
        
        istream = new ObjectInputStream(br);
        String ss1 = (String) istream.readObject();
        
        if (result instanceof String)
        { String content = (String) result; 
          // doc.insertString(0, content, attrs[0]);
          // return; 
        
          for (int i = 0; i < ss1.length() && i < content.length(); i++) 
          { char c = ss1.charAt(i); 
            if (c == 'p')
            { doc.insertString(i, "" + content.charAt(i), attrs[0]); }
            else if (c == '^')
            { doc.insertString(i, "" + content.charAt(i), attrs[5]); }
            else if (c == '_')
            { doc.insertString(i, "" + content.charAt(i), attrs[4]); }
          }
        }
      }
      catch (Exception _e)
      { System.out.println("!! Problem loading data");
        _e.printStackTrace();
        return; 
      }

      thisLabel.setText("Specification loaded from Test.xml, data.ser");
    } 
  } 

  class CheckAction extends javax.swing.AbstractAction
  { public CheckAction()
    { super("Check"); }

    public void actionPerformed(ActionEvent e)
    { int pos = doc.getLength();

      String result = ""; 
      String style = "plain";  
          
      for (int i = 0; i < pos; i++)
      { try 
        { textPane.setCaretPosition(i);
          String txt = textPane.getText(i,1);
          char c1 = txt.charAt(0); 
          if (encodedChars.contains(c1))
          { txt = "" + charMap.get(c1); } 
  
          AttributeSet st = textPane.getCharacterAttributes();

          if (st.isEqual(attrs[0]))
          { if (style.equals("subscript") || 
                style.equals("superscript"))
            { result = result + "}" + txt; } 
            else 
            { result = result + txt; }
            style = "plain"; 
          } 
          else if (st.isEqual(attrs[4]))
          { if (style.equals("plain"))
            { result = result + "_{" + txt; } 
            else if (style.equals("superscript"))
            { result = result + "}_{" + txt; } 
            else 
            { result = result + txt; }
            style = "subscript"; 
          } 
          else if (st.isEqual(attrs[5]))
          { if (style.equals("plain"))
            { result = result + "^{" + txt; } 
            else if (style.equals("subscript"))
            { result = result + "}^{" + txt; } 
            else 
            { result = result + txt; }
            style = "superscript"; 
          } 

          System.out.println(txt + " " + style);
        } catch (Exception ex) { }  
      }  
      messageArea.setText(result);
      internalModel = result;
      ASTTerm.mathocltheorems = new Vector();  
      ASTTerm.mathoclrewrites = new Vector();  
          
      thisLabel.setText("Specification translated to text format");
    }
  }

  class AnalyseAction extends javax.swing.AbstractAction
  { public AnalyseAction()
    { super("Analyse"); }

    public void actionPerformed(ActionEvent e)
    { String result = messageArea.getText(); 
          // internalModel; 

      if (result == null || result.trim().length() == 0)
      { System.err.println("!! Error: input text cannot be empty. Run 'Check' option first!"); 
        return; 
      } 

      String[] args = {"MathOCL", "specification"}; 

      try { 
        org.antlr.v4.gui.AntlrGUI antlr = 
          new org.antlr.v4.gui.AntlrGUI(args); 

        antlr.setText(result); 

        antlr.process(); 

        String asttext = antlr.getResultText(); 
        // messageArea.setText("" + asttext);
        // System.out.println(asttext); 
 
        Compiler2 cc = new Compiler2(); 
        ASTTerm trm = cc.parseMathOCLAST(asttext); 
        if (trm != null && trm instanceof ASTCompositeTerm)  
        { ASTCompositeTerm spec = (ASTCompositeTerm) trm;
          ASTTerm.mathoclvars = new java.util.HashMap();  
          ASTTerm.mathoclrewrites = new Vector(); 
          ASTTerm.mathoclfunctionIndex = 1;  
          spec.checkMathOCL(); 

          // String extracode = 
          //   spec.preprocessMathOCL(); 

          Vector ents = new Vector(); 
          Vector typs = new Vector(); 
          CGSpec cgs = new CGSpec(entities,types); 
          File fs = new File("cg/simplify.cstl"); 
          CSTL.loadCSTL(cgs,fs,ents,typs);
 
          long t1 = (new java.util.Date()).getTime(); 

          String entcode = trm.cg(cgs);

          System.out.println(entcode);

          // messageArea.append("\n"); 
          long t2 = (new java.util.Date()).getTime(); 

          System.out.println(">>> Processing took " + (t2 - t1)); 

          messageArea.setText(entcode);
          internalModel = entcode;   
        } 
      } 
      catch (Exception _expt) 
      { _expt.printStackTrace(); } 

      thisLabel.setText("Specification analysed & simplified");
    }
  }

  class MatlabAction extends javax.swing.AbstractAction
  { public MatlabAction()
    { super("Generate Matlab"); }

    public void actionPerformed(ActionEvent e)
    { String result = internalModel; 

      String[] args = {"MathOCL", "specification"}; 

      try { 
        org.antlr.v4.gui.AntlrGUI antlr = 
          new org.antlr.v4.gui.AntlrGUI(args); 

        antlr.setText(result); 

        antlr.process(); 

        String asttext = antlr.getResultText(); 
        // messageArea.setText("" + asttext);
        System.out.println(asttext); 
 
        Compiler2 cc = new Compiler2(); 
        ASTTerm trm = cc.parseMathOCLAST(asttext); 
        if (trm != null)  
        { 
          Vector ents = new Vector(); 
          Vector typs = new Vector(); 
          CGSpec cgs = new CGSpec(entities,types); 
          File fs = new File("cg/mathocl2matlab.cstl"); 
          CSTL.loadCSTL(cgs,fs,ents,typs);
 
          String entcode = trm.cg(cgs);

          System.out.println(entcode);
          // messageArea.append("\n"); 
          // messageArea.append(entcode);
          // internalModel = entcode;   
        } 
      } 
      catch (Exception _expt) 
      { _expt.printStackTrace(); } 
      thisLabel.setText("Translated to Matlab");
    }
  }

  class KM3Action extends javax.swing.AbstractAction
  { public KM3Action()
    { super("Generate UML/OCL"); }

    public void actionPerformed(ActionEvent e)
    { String result = internalModel; 

      if (umlPane != null) 
      { umlPane.setVisible(true); }
      else 
      {  
        umlPane = new JEditorPane();  
        umlPane.setEditable(false); 
        umlPane.setSize(400,400);
        int w = getWidth(); 
        int h = getHeight(); 
 
        getContentPane().add(new JScrollPane(umlPane),  
                             java.awt.BorderLayout.EAST); 
        setSize(w + 400, h); 
        umlPane.setVisible(true); 

        java.awt.LayoutManager ll = getLayout(); 
        if (ll != null)
        { ll.layoutContainer(getContentPane()); }  

        umlPane.repaint(); 
        repaint(); 
      } 

      String[] args = {"MathOCL", "specification"}; 

      try { 
        org.antlr.v4.gui.AntlrGUI antlr = 
          new org.antlr.v4.gui.AntlrGUI(args); 

        antlr.setText(result); 

        antlr.process(); 

        String asttext = antlr.getResultText(); 
        // messageArea.setText("" + asttext);
        System.out.println(asttext); 
 
        Compiler2 cc = new Compiler2(); 
        ASTTerm trm = cc.parseMathOCLAST(asttext); 
        if (trm != null)  
        { 
          Vector ents = new Vector(); 
          Vector typs = new Vector(); 
          CGSpec cgs = new CGSpec(entities,types); 
          File fs = new File("cg/mathocl2ocl.cstl"); 
          CSTL.loadCSTL(cgs,fs,ents,typs);
 
          String entcode = trm.cg(cgs);

          String arg1 = CGRule.correctNewlines(entcode); 
          System.out.println(arg1); 

          File datelib = new File("libraries/ocldate.km3"); 
          if (datelib.exists())
          { loadKM3FromFile(datelib); }
          else 
          { System.err.println("! Warning: no file libraries/ocldate.km3"); } 

          File mathlib = new File("libraries/mathlib.km3"); 
          if (mathlib.exists())
          { loadKM3FromFile(mathlib); }
          else 
          { System.err.println("! Warning: no file libraries/mathlib.km3"); } 

          File randomlib = new File("libraries/oclrandom.km3"); 
          if (randomlib.exists())
          { loadKM3FromFile(randomlib); }
          else 
          { System.err.println("! Warning: no file libraries/oclrandom.km3"); } 

          Compiler2 comp = new Compiler2(); 
          comp.nospacelexicalanalysis("package app {\n " + arg1 + "\n}\n\n"); 
          Vector yentities = comp.parseKM3();

          entities.addAll(yentities); 

          for (int k = 0; k < yentities.size(); k++) 
          { Entity ent = (Entity) yentities.get(k); 
            ent.typeCheck(types,entities); 
          } 
 
          umlPane.setText(entcode); 
          // System.out.println(entcode);
          // messageArea.append("\n"); 
          // messageArea.append(entcode);
          // internalModel = entcode;   
        } 
      } 
      catch (Exception _expt) 
      { _expt.printStackTrace(); } 
      thisLabel.setText("Translated to UML/OCL");
    }
  }

  class MambaAction extends javax.swing.AbstractAction
  { public MambaAction()
    { super("Generate Mamba"); }

    public void actionPerformed(ActionEvent e)
    { String result = internalModel; 

      String[] args = {"MathOCL", "specification"}; 

      try { 
        org.antlr.v4.gui.AntlrGUI antlr = 
          new org.antlr.v4.gui.AntlrGUI(args); 

        antlr.setText(result); 

        antlr.process(); 

        String asttext = antlr.getResultText(); 
        // messageArea.setText("" + asttext);
        System.out.println(asttext); 
 
        Compiler2 cc = new Compiler2(); 
        ASTTerm trm = cc.parseMathOCLAST(asttext); 
        if (trm != null)  
        { 
          Vector ents = new Vector(); 
          Vector typs = new Vector(); 
          CGSpec cgs = new CGSpec(entities,types); 
          File fs = new File("cg/mathocl2mamba.cstl"); 
          CSTL.loadCSTL(cgs,fs,ents,typs);
 
          String entcode = trm.cg(cgs);

          String arg1 = CGRule.correctNewlines(entcode); 
          System.out.println(arg1); 

          // messageArea.setText(arg1);
          // internalModel = entcode;   
        } 
      } 
      catch (Exception _expt) 
      { _expt.printStackTrace(); } 
      thisLabel.setText("Translated to Mamba");
    }
  }

  class Translate2JavaAction extends javax.swing.AbstractAction
  { public Translate2JavaAction()
    { super("Translate to Java"); }

    public void actionPerformed(ActionEvent e)
    { if (entities.size() == 0) 
      { System.err.println("!! No classes exist: translate to UML/OCL before using this option!"); 
        return; 
      }

      StringWriter sw = new StringWriter(); 
      PrintWriter out = new PrintWriter(sw);   
      for (int i = 0; i < entities.size(); i++) 
      { Entity ent = (Entity) entities.get(i);
        if (ent.isExternal() || ent.isComponent()) 
        { continue; }  
        ent.generateJava7(entities,types,out);     
      } 
      String res = sw.toString(); 
      // messageArea.setText(res);
      System.out.println(res); 
      thisLabel.setText("Translated to Java");
    } 
  } 


  class Translate2CSAction extends javax.swing.AbstractAction
  { public Translate2CSAction()
    { super("Translate to C#"); }

    public void actionPerformed(ActionEvent e)
    { StringWriter sw = new StringWriter(); 
      PrintWriter out = new PrintWriter(sw);

      if (entities.size() == 0) 
      { System.err.println("!! No classes exist: translate to UML/OCL before using this option!"); 
        return; 
      } 
   
      for (int i = 0; i < entities.size(); i++) 
      { Entity ent = (Entity) entities.get(i);
        if (ent.isExternal() || ent.isComponent()) 
        { continue; }  
        ent.generateCSharp(entities,types,out);     
      } 

      String res = sw.toString();
      System.out.println(res);  
      // messageArea.setText(res);
      thisLabel.setText("Translated to C#");
    } 
  }

  class Translate2CPPAction extends javax.swing.AbstractAction
  { public Translate2CPPAction()
    { super("Translate to C++"); }

    public void actionPerformed(ActionEvent e)
    { if (entities.size() == 0) 
      { System.err.println("!! No classes exist: translate to UML/OCL before using this option!"); 
        return; 
      } 

      StringWriter sw = new StringWriter(); 
      PrintWriter out = new PrintWriter(sw);   
      
      StringWriter sw1 = new StringWriter(); 
      PrintWriter out1 = new PrintWriter(sw1);   
      
      for (int i = 0; i < entities.size(); i++) 
      { Entity ent = (Entity) entities.get(i);
        if (ent.isExternal() || ent.isComponent()) 
        { continue; }  
        ent.generateCPP(entities,types,out,out1);     
      } 
      String res = sw.toString(); 
      String res1 = sw1.toString(); 
      // messageArea.setText(res + "\n\n" + res1);
      System.out.println(res + "\n\n" + res1);
      thisLabel.setText("Translated to C++");
    } 
  }

  class Translate2PythonAction extends javax.swing.AbstractAction
  { public Translate2PythonAction()
    { super("Translate to Python"); }

    public void actionPerformed(ActionEvent e)
    { if (entities.size() == 0) 
      { System.err.println("!! No classes exist: translate to UML/OCL before using this option!"); 
        return; 
      }

      saveModelToFile(types, entities, "output/model.txt"); 

      RunApp rapp1 = new RunApp("uml2py"); 

      try
      { rapp1.setFile("app.py"); 
        Thread appthread = new Thread(rapp1); 
        appthread.start(); 
      } 
      catch (Exception ee2) 
      { System.err.println("!! Unable to run uml2py.jar"); } 

      StringWriter sw = new StringWriter(); 
      PrintWriter out = new PrintWriter(sw);   
      for (int i = 0; i < entities.size(); i++) 
      { Entity ent = (Entity) entities.get(i);
        if (ent.isExternal() || ent.isComponent()) 
        { continue; }  
        ent.generateJava7(entities,types,out);     
      } 
      String res = sw.toString(); 
      // messageArea.setText(res);
      System.out.println(res); 
      thisLabel.setText("Translated to Python");
    } 
  } 


  class HelpNotationAction extends javax.swing.AbstractAction
  { public HelpNotationAction()
    { super("Notation help"); }

    public void actionPerformed(ActionEvent e)
    { if (helpPane != null) 
      { helpPane.setVisible(true); }
      else 
      {  
        helpPane = new JEditorPane();  
        helpPane.setEditable(false); 
        helpPane.setSize(300,400);
        int w = getWidth(); 
        int h = getHeight(); 
 
        getContentPane().add(new JScrollPane(helpPane),  
                             java.awt.BorderLayout.EAST); 
        setSize(w + 300, h); 
        helpPane.setVisible(true); 

        java.awt.LayoutManager ll = getLayout(); 
        if (ll != null)
        { ll.layoutContainer(getContentPane()); }  

        helpPane.repaint(); 
        repaint(); 
      } 
 
      helpPane.setText("Specifications contain these elements: \n\n" + 
        "1. Define clauses, with syntax one of\n" + 
        "    Define var = expr\n" +
        "    Define var : type\n" + 
        "    Define var : type = expr\n" + 
        "    Define funcn(pars) = expr\n" + 
        "    Define var = instruction\n" + 
        "    Define var ~ distribution\n\n" +
        "2. Constraint clauses, with syntax\n" + 
        "    Constraint on var | expr\n\n" + 
        "3. Solve clauses, with syntax\n" + 
        "    Solve eqn(s) for var(s)\n\n" + 
        "4. Simplify clauses: \n" + 
        "    Simplify expr\n\n" + 
        "5. Prove clauses:\n" + 
        "    Prove expr if expr\n\n" + 
        "6. Theorem/rewrite rule clauses:\n" + 
        "    Theorem expr when expr\n" + 
        "    Rewrite expr to expr\n\n" + 
        "Instructions can be one of:\n" + 
        "    Factor expr by expr\n" + 
        "    Cancel expr in expr\n" + 
        "    Substitute var in expr\n" + 
        "    Group expr by var\n" + 
        "    Expand expr to N terms\n" + 
        "    Express expr as polynomial in var\n\n" + 
        "Distributions are:\n" + 
        "    N(mu,sigma^2), Bernoulli(mu), Binom(n,p),\n" + 
        "    U(), U(a,b), Poisson(mu), LogNorm(mu,sigma^2)\n"); 

        helpPane.repaint(); 
        repaint();  
      } 
  } 

  class HelpProcessAction extends javax.swing.AbstractAction
  { public HelpProcessAction()
    { super("Process help"); }

    public void actionPerformed(ActionEvent e)
    { if (helpPane != null) 
      { helpPane.setVisible(true); }
      else 
      {  
        helpPane = new JEditorPane();  
        helpPane.setEditable(false); 
        helpPane.setSize(300,400);
        int w = getWidth(); 
        int h = getHeight(); 
 
        getContentPane().add(new JScrollPane(helpPane),  
                             java.awt.BorderLayout.EAST); 
        setSize(w + 300, h); 
        helpPane.setVisible(true); 

        java.awt.LayoutManager ll = getLayout(); 
        if (ll != null)
        { ll.layoutContainer(getContentPane()); }  

        helpPane.repaint(); 
        repaint(); 
      } 
 
      helpPane.setText("Specifications are processed by: \n\n" + 
        "1. Check -- translates mathematical notation to\n" + 
        "    ASCII in the lower pane\n\n" +
        "2. Analyse -- executes MathOCL parser and\n" + 
        "    simplify.cstl to simplify expressions, solve\n" + 
        "    equations and attempt proofs.\n" + 
        "    Individual quadratic & homogenous differential\n" +
        "    equations can be solved, eg: \n" + 
        "      Solve 2*(f" + '\u2032' + ") - f = 0 for f\n" + 
        "    Also multiple linear equations.\n" + 
        "    The re-written specification is put in the lower\n" + 
        "    pane. Analyse can be applied repeatedly and\n" + 
        "    instructions can be inserted in the text.\n\n" + 
        "3. Translate -- to UML/OCL and then to a\n" + 
        "    programming language, or to Mamba or Matlab.\n" +
        "    Specification should contain only Define,\n" + 
        "    Simplify and Constraint elements.\n" +  
        "    Uses mathocl2ocl.cstl, mathocl2mamba.cstl,\n" + 
        "    mathocl2matlab.cstl, and AgileUML code\n" + 
        "    generators for Java, C#, C++\n"); 

        helpPane.repaint(); 
        repaint();  
      } 
  } 

 class HelpOCLAction extends javax.swing.AbstractAction
  { public HelpOCLAction()
    { super("OCL help"); }

    public void actionPerformed(ActionEvent e)
    { if (helpPane != null) 
      { helpPane.setVisible(true); }
      else 
      {  
        helpPane = new JEditorPane();  
        helpPane.setEditable(false); 
        helpPane.setSize(300,400);
        int w = getWidth(); 
        int h = getHeight(); 
 
        getContentPane().add(new JScrollPane(helpPane),  
                             java.awt.BorderLayout.EAST); 
        setSize(w + 300, h); 
        helpPane.setVisible(true); 

        java.awt.LayoutManager ll = getLayout(); 
        if (ll != null)
        { ll.layoutContainer(getContentPane()); }  

        helpPane.repaint(); 
        repaint(); 
      } 
 
      helpPane.setText("MathOCL contains Integer, Real & Boolean\n" + 
        "types & expressions from OCL: \n\n" + 
        "Types:\n" + 
        "   Integer is " + '\u2124' + "\n" + 
        "   Real is " + '\u211D' + "\n" + 
        "Expressions:\n" + 
        "   Operators +, -, *, /, <, >, <=, >=, =, /=\n" + 
        "   Numbers in usual formats, together with\n" + 
        "   " + '\u221E' + " for Math_PINFINITY,\n" + 
        "   -" + '\u221E' + " for Math_NINFINITY,\n" +
        "   ? for Math_NaN.\n" + 
        "   Functions are written as f(x). Eg: sin(x)\n" +
        "   instead of x->sin().\n" + 
        "   Powers and exponents are written with superscripts.\n\n" +
        "   Boolean values are true, false, with operators\n" + 
        "   &, or, =>, not\n" + 
        "   " + '\u018E' + " x : s " + '\u2219' + " expr expresses \n" + 
        "   s->exists( x | expr )\n" + 
        "   " + '\u2200' + " x : s " + '\u2219' + " expr expresses \n" + 
        "   s->forAll( x | expr )\n\n"
        ); 

        helpPane.repaint(); 
        repaint();  
      } 
  } 

  /* Solve 2*(f?) - f = 0 for f */ 


  public void saveModelToFile(Vector typs, Vector ents, String f)
  { File file = new File(f);
    try
    { PrintWriter out =
          new PrintWriter(
            new BufferedWriter(new FileWriter(file)));

      UCDArea.saveBasicTypes(out); 

      for (int i = 0; i < typs.size(); i++) 
      { ModelElement tt = (ModelElement) typs.get(i); 
        tt.asTextModel(out); 
      } 

      for (int i = 0; i < ents.size(); i++) 
      { Entity ent = (Entity) ents.get(i); 
        ent.asTextModel(out); 
      } 

      // generalisations also 

      out.close(); 
    } catch (Exception _ex) 
      { System.err.println("!! Cannot save model to " + f); } 
  }


  public static void main(String[] args) {
     MathApp window = new MathApp();
     window.setTitle("MathOCL Editor");
     window.setSize(800, 600);
     window.setVisible(true);   
  } 

}

class PositionTextAction extends StyledEditorKit.StyledTextAction
{ JTextPane textEditor; 
  AttributeSet attrs; 
  MathApp parent; 

  PositionTextAction(String nme, MathApp owner) 
  { super(nme);
    parent = owner; 
  } 

  void setAttributes(JTextPane ed, AttributeSet attr)
  { textEditor = ed;
    attrs = attr;  
    super.setCharacterAttributes(ed, attr, false); 
  }

  public void actionPerformed(ActionEvent e)
  { textEditor.setCharacterAttributes(attrs, true);
    parent.currentAttrs = (SimpleAttributeSet) attrs; 
  } 
}


