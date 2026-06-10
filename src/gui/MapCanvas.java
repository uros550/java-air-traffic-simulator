package gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import models.AviationCenter;

public class MapCanvas extends Canvas {
    
    private final AviationCenter center;

    public MapCanvas(AviationCenter center) {
        this.center = center;
    }

    @Override
    public void paint(Graphics g) {
        //actual simulation will be happening here
        g.setColor(Color.WHITE);
        g.drawString("MAP ENGINE READY - AWAITING PHASE B", 20, 30);
    }
    
}