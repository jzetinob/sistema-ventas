/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Campo de texto con búsqueda por autocompletado.
 *
 * @author josue zetino
 */
public class CampoBusqueda extends javax.swing.JPanel {

    private static final int MAX_SUGERENCIAS = 15;

    private final JTextField campo = new JTextField();
    private final JList<String> listaSugerencias = new JList<>();
    private final JPopupMenu menuSugerencias = new JPopupMenu();
    private final List<ActionListener> oyentes = new ArrayList<>();
    private List<String> elementos = new ArrayList<>();
    private boolean sinCoincidencias = false;
    private java.util.function.Supplier<List<String>> fuente;

    public CampoBusqueda() {
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());
        add(campo, java.awt.BorderLayout.CENTER);

        listaSugerencias.setFocusable(false);
        listaSugerencias.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listaSugerencias.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int indice = listaSugerencias.locationToIndex(e.getPoint());
                if (indice != -1) {
                    listaSugerencias.setSelectedIndex(indice);
                    elegir();
                }
            }
        });

        menuSugerencias.setFocusable(false);
        campo.putClientProperty("JTextField.placeholderText", "Escriba para buscar…");
        JScrollPane scroll = new JScrollPane(listaSugerencias);
        scroll.setPreferredSize(new Dimension(320, 150));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        menuSugerencias.add(scroll);

        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarSugerencias();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarSugerencias();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarSugerencias();
            }
        });

        campo.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // datos al dia: pudieron registrarse en otra ventana o desde la app movil
                if (fuente != null) {
                    elementos = new ArrayList<>(fuente.get());
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                menuSugerencias.setVisible(false);
            }
        });

        campo.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "siguiente");
        campo.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "anterior");
        campo.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "elegir");
        campo.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        campo.getActionMap().put("siguiente", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (menuSugerencias.isVisible()) {
                    int indice = Math.min(listaSugerencias.getSelectedIndex() + 1, listaSugerencias.getModel().getSize() - 1);
                    listaSugerencias.setSelectedIndex(indice);
                    listaSugerencias.ensureIndexIsVisible(indice);
                } else {
                    mostrarSugerencias();
                }
            }
        });
        campo.getActionMap().put("anterior", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (menuSugerencias.isVisible()) {
                    int indice = Math.max(listaSugerencias.getSelectedIndex() - 1, 0);
                    listaSugerencias.setSelectedIndex(indice);
                    listaSugerencias.ensureIndexIsVisible(indice);
                }
            }
        });
        campo.getActionMap().put("elegir", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (menuSugerencias.isVisible() && listaSugerencias.getSelectedIndex() != -1) {
                    elegir();
                } else {
                    notificar();
                }
            }
        });
        campo.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuSugerencias.setVisible(false);
            }
        });
    }

    public void setElementos(List<String> elementos) {
        this.elementos = new ArrayList<>(elementos);
    }

    /** De donde se leen los elementos; se vuelve a consultar cada vez que el campo recibe el foco. */
    public void setFuente(java.util.function.Supplier<List<String>> fuente) {
        this.fuente = fuente;
        setElementos(fuente.get());
    }

    public String getText() {
        return campo.getText();
    }

    public void setText(String texto) {
        campo.setText(texto);
    }

    public void addActionListener(ActionListener oyente) {
        oyentes.add(oyente);
    }

    private void actualizarSugerencias() {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            menuSugerencias.setVisible(false);
            return;
        }
        String minusculas = texto.toLowerCase();
        List<String> coincidencias = new ArrayList<>();
        for (String elemento : elementos) {
            if (elemento.toLowerCase().contains(minusculas)) {
                coincidencias.add(elemento);
                if (coincidencias.size() >= MAX_SUGERENCIAS) {
                    break;
                }
            }
        }
        sinCoincidencias = coincidencias.isEmpty();
        if (sinCoincidencias) {
            // se avisa en lugar de no mostrar nada, para que se note que el buscador funciona
            coincidencias.add(elementos.isEmpty() ? "(el catálogo está vacío)" : "(sin coincidencias)");
        }
        listaSugerencias.setListData(coincidencias.toArray(new String[0]));
        listaSugerencias.setSelectedIndex(sinCoincidencias ? -1 : 0);
        if (!menuSugerencias.isVisible()) {
            mostrarSugerencias();
        }
    }

    private void mostrarSugerencias() {
        menuSugerencias.show(this, 0, getHeight());
    }

    private void elegir() {
        if (sinCoincidencias) {
            menuSugerencias.setVisible(false);
            return;
        }
        String valor = listaSugerencias.getSelectedValue();
        if (valor != null) {
            campo.setText(valor);
        }
        menuSugerencias.setVisible(false);
        notificar();
    }

    private void notificar() {
        ActionEvent evento = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "seleccion");
        for (ActionListener oyente : oyentes) {
            oyente.actionPerformed(evento);
        }
    }
}
