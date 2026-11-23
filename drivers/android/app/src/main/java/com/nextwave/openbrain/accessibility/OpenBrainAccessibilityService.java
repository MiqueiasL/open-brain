package com.nextwave.openbrain.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenBrain Accessibility Service
 * O superpoder que lê a árvore de interface de qualquer aplicativo e injeta toques/rolagens,
 * permitindo automação total mesmo em aplicativos que não possuem APIs públicas.
 */
public class OpenBrainAccessibilityService extends AccessibilityService {
    private static final String TAG = "OpenBrainA11y";
    private static OpenBrainAccessibilityService instance;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.i(TAG, "🟢 OpenBrain Accessibility Service CONECTADO e operacional!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Eventos de janela / toque recebidos em tempo real
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "⚠️ OpenBrain Accessibility Service interrompido.");
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        instance = null;
        return super.onUnbind(intent);
    }

    public static OpenBrainAccessibilityService getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    /**
     * Extrai todo o texto visível da tela atual em uma lista formatada
     */
    public List<String> getVisibleScreenText() {
        List<String> texts = new ArrayList<>();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return texts;

        traverseNodes(root, (node) -> {
            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();
            if (text != null && text.length() > 0) {
                texts.add(text.toString().trim());
            } else if (desc != null && desc.length() > 0) {
                texts.add("[" + desc.toString().trim() + "]");
            }
        });

        return texts;
    }

    /**
     * Localiza e clica em um botão ou elemento pelo seu texto visível
     */
    public boolean clickByText(String targetText) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(targetText);
        if (nodes == null || nodes.isEmpty()) return false;

        for (AccessibilityNodeInfo node : nodes) {
            if (performClick(node)) {
                Log.i(TAG, "✅ Clique efetuado no elemento com texto: " + targetText);
                return true;
            }
        }
        return false;
    }

    /**
     * Localiza e clica em um elemento pelo seu View ID
     */
    public boolean clickById(String viewId) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(viewId);
        if (nodes == null || nodes.isEmpty()) return false;

        for (AccessibilityNodeInfo node : nodes) {
            if (performClick(node)) {
                Log.i(TAG, "✅ Clique efetuado no elemento com ID: " + viewId);
                return true;
            }
        }
        return false;
    }

    /**
     * Rola a tela para frente (baixo)
     */
    public boolean scrollForward() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        final boolean[] scrolled = {false};
        traverseNodes(root, (node) -> {
            if (!scrolled[0] && node.isScrollable()) {
                scrolled[0] = node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
        });
        return scrolled[0];
    }

    /**
     * Preenche texto no campo editável focado
     */
    public boolean inputText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focused != null && focused.isEditable()) {
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        }
        return false;
    }

    private boolean performClick(AccessibilityNodeInfo node) {
        if (node == null) return false;
        if (node.isClickable()) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        AccessibilityNodeInfo parent = node.getParent();
        if (parent != null) {
            boolean clicked = performClick(parent);
            parent.recycle();
            return clicked;
        }
        return false;
    }

    private interface NodeVisitor {
        void visit(AccessibilityNodeInfo node);
    }

    private void traverseNodes(AccessibilityNodeInfo root, NodeVisitor visitor) {
        if (root == null) return;
        visitor.visit(root);
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                traverseNodes(child, visitor);
                child.recycle();
            }
        }
    }
}
