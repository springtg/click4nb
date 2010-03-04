/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click.editor.hyperlinks;

import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.click.editor.ClickEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author hantsy
 */
public class ResourceHyperlinkProvider implements HyperlinkProvider {

    private final static List<String> supportedResource = Arrays.asList(
            new String[]{"CssImport", "JsImport"});
    int startOffset;
    int endOffset;
    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = java.util.logging.Logger.getLogger("org.netbeans.modules.web.click.editor.hyperlinks.ResourceHyperlinkProvider");
        org.netbeans.modules.web.click.editor.hyperlinks.ResourceHyperlinkProvider.initLoggerHandlers();
    }

    private static final void initLoggerHandlers() {
        java.util.logging.Handler[] handlers = LOGGER.getHandlers();
        boolean hasConsoleHandler = false;
        for (java.util.logging.Handler handler : handlers) {
            if (handler instanceof java.util.logging.ConsoleHandler) {
                hasConsoleHandler = true;
            }
        }
        if (!hasConsoleHandler) {
            LOGGER.addHandler(new java.util.logging.ConsoleHandler());
        }
        LOGGER.setLevel(java.util.logging.Level.FINEST);
    }
    private FileObject fileToOpen;
    private String linkTarget;

    @Override
    public boolean isHyperlinkPoint(Document document, int offset) {
        if (!(document instanceof BaseDocument)) {
            return false;
        }

        BaseDocument doc = (BaseDocument) document;
        JTextComponent target = Utilities.getFocusedComponent();

        if ((target == null) || (target.getDocument() != doc)) {
            return false;
        }


        try {
            TokenHierarchy<String> hi = TokenHierarchy.create(doc.getText(0, doc.getLength()), JavaTokenId.language());
            TokenSequence<JavaTokenId> ts = hi.tokenSequence(JavaTokenId.language());

            ts.move(offset);
            boolean lastTokenInDocument = !ts.moveNext();
            if (lastTokenInDocument) {
                // end of the document
                return false;
            }

            Token<JavaTokenId> identifierToken = ts.offsetToken();

            do {
                // find '='
                ts.movePrevious();
            } while (ts.token() != null
                    && ts.token().id() == JavaTokenId.WHITESPACE); // whitespace

            do {
                // find 'beanclass'
                ts.movePrevious();
            } while (ts.token() != null && ts.token().id() == JavaTokenId.WHITESPACE); // whitespace

            Token<JavaTokenId> resourceToken = ts.token();


            if (null == identifierToken
                    || null == resourceToken || identifierToken.length() <= 2) {
                return false;
            }

            if (null != identifierToken
                    && null != resourceToken
                    && matcheSupportedResources(resourceToken.text().toString())
                    && identifierToken.id() == JavaTokenId.STRING_LITERAL && // identified must be string
                    identifierToken.length() > 2) { // identifier must be longer than "" string
                startOffset = identifierToken.offset(hi) + 1;
                endOffset = identifierToken.offset(hi) + identifierToken.length() - 1;

                if (startOffset > endOffset) {
                    endOffset = startOffset;
                }

                linkTarget = identifierToken.text().subSequence(1, identifierToken.length() - 1).toString();
                LOGGER.finest("Hyperlink at: " + startOffset + "-" + endOffset
                        + ": " + linkTarget);
                return true;
            }

        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private FileObject findResourceFileObject(Document doc, String path) {
        FileObject docFO = NbEditorUtilities.getFileObject(doc);
        WebModule wm = WebModule.getWebModule(docFO);
        if (wm == null) {
            return null;
        }
        FileObject docBase = wm.getDocumentBase();
        return docBase.getFileObject(path);
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int position) {
        return new int[]{startOffset, endOffset};
    }

    @Override
    public void performClickAction(Document doc, int position) {
        fileToOpen = findResourceFileObject(doc, linkTarget);
        if (fileToOpen == null) {
            StatusDisplayer.getDefault().setStatusText("Invalid path :" + linkTarget);
            return;
        }
        ClickEditorUtilities.openInEditor(fileToOpen);
    }

    private boolean matcheSupportedResources(String resource) {
        for (String supportedResolution : supportedResource) {
            if (resource.contains(supportedResolution)) {
                return true;
            }
        }

        return false;
    }
}
