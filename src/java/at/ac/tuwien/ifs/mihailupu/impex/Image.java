/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.io.Serializable;

/**
 *
 * @author mihailupu
 */
public class Image implements Serializable {

    String path;
    String style;
    boolean isCandidateSelected;
    boolean isGoldDuplicate;
    boolean isGoldMatch;
    boolean isGoldSupraimage;
    boolean isGoldSubimage;
    boolean isCandidateError;
    boolean visible=true;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Image other = (Image) obj;
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.path != null ? this.path.hashCode() : 0);
        return hash;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    

    public boolean isIsCandidateError() {
        return isCandidateError;
    }

    public void setIsCandidateError(boolean isCandidateError) {
        this.isCandidateError = isCandidateError;
    }

    public boolean isIsCandidateSelected() {
        return isCandidateSelected;
    }

    public void setIsCandidateSelected(boolean isCandidateSelected) {
        this.isCandidateSelected = isCandidateSelected;
    }

    public boolean isIsGoldDuplicate() {
        return isGoldDuplicate;
    }

    public void setIsGoldDuplicate(boolean isGoldDuplicate) {
        this.isGoldDuplicate = isGoldDuplicate;
    }

    public boolean isIsGoldMatch() {
        return isGoldMatch;
    }

    public void setIsGoldMatch(boolean isGoldMatch) {
        this.isGoldMatch = isGoldMatch;
    }

    public boolean isIsGoldSubimage() {
        return isGoldSubimage;
    }

    public void setIsGoldSubimage(boolean isGoldSubimage) {
        this.isGoldSubimage = isGoldSubimage;
    }

    public boolean isIsGoldSupraimage() {
        return isGoldSupraimage;
    }

    public void setIsGoldSupraimage(boolean isGoldSupraimage) {
        this.isGoldSupraimage = isGoldSupraimage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Image(String path, String style) {
        this.path = path;
        this.style = style;
        isCandidateSelected = false;
        isGoldDuplicate = false;
        isGoldMatch = false;
        isGoldSupraimage = false;
        isGoldSubimage = false;
        isCandidateError = false;
    }
}
