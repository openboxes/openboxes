/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.util

import javax.imageio.ImageIO as IIO
import javax.swing.*
import java.awt.*
import java.awt.Image as AWTImage
import java.awt.image.BufferedImage

class ImageUtil {

    def resizeImage = { bytes, out, maxW, maxH ->
        AWTImage ai = new ImageIcon(bytes).image
        int width = ai.getWidth(null)
        int height = ai.getHeight(null)

        float aspectRatio = width / height
        float requiredAspectRatio = maxW / maxH

        int dstW = 0
        int dstH = 0
        if (requiredAspectRatio < aspectRatio) {
            dstW = maxW
            dstH = Math.round(maxW / aspectRatio)
        } else {
            dstH = maxH
            dstW = Math.round(maxH * aspectRatio)
        }

        BufferedImage bi = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_RGB)
        Graphics2D g2d = bi.createGraphics()
        g2d.drawImage(ai, 0, 0, dstW, dstH, null, null)

        IIO.write(bi, 'JPEG', out)
    }

}