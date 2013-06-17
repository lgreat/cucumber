package gs.web.ads;

import org.apache.commons.lang.StringUtils;

public class AdSize {
    final public static AdSize Size_300x250 = new AdSize(300,250);
    final public static AdSize Size_300x600 = new AdSize(300,600);
    final public static AdSize Size_630x40 = new AdSize(630,40);
    final public static AdSize Size_630x145 = new AdSize(630,145);
    final public static AdSize Size_150x30 = new AdSize(150,30);

    public AdSize getCompanionSize() {
        if (this.equals(Size_300x250)) {
            return Size_300x600;
        } else if (this.equals(Size_300x600)) {
            return Size_300x250;
        } else if (this.equals(Size_630x40)) {
            return Size_630x145;
        } else if (this.equals(Size_630x145)) {
            return Size_630x40;
        } else if (this.equals(Size_150x30)) {
            return Size_150x30;
        }
        return null;
    }

    private int _width;
    private int _height;

    public AdSize(String baseName) {
        initWidthHeight(baseName);
    }

    public AdSize(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Width and height must be 1 or higher");
        }
        _width = width;
        _height = height;
    }

    private void initWidthHeight(String baseName) {
        Integer width = null;
        Integer height = null;

        if (baseName != null) {
            String[] tokens = baseName.split("_");
            for (String token : tokens) {
                if (token.contains("x")) {
                    String[] possibleDimensions = token.split("x");
                    if (possibleDimensions.length == 2) {
                        if (StringUtils.isNumeric(possibleDimensions[0]) &&
                                StringUtils.isNumeric(possibleDimensions[1])) {
                            try {
                                width = Integer.parseInt(possibleDimensions[0]);
                                height = Integer.parseInt(possibleDimensions[1]);
                            } catch (NumberFormatException e) {
                                // this should never happen
                            }
                        }
                    }
                }
            }
        }

        // make sure that there's at least a 1x1 pixel for each ad
        if (width != null && width > 0) {
            _width = width;
        } else {
            _width = 1;
        }
        if (height != null && height > 0) {
            _height = height;
        } else {
            _height = 1;
        }
    }

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdSize adSize = (AdSize) o;

        if (_height != adSize._height) return false;
        if (_width != adSize._width) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _width;
        result = 31 * result + _height;
        return result;
    }
}