#http://code.activestate.com/recipes/325823/
# modified by myself

class Scene(object):
    def __init__(self, name="svg", height=400, width=400):
        self.name = name
        self.items = []
        self.height = height
        self.width = width
        return

    def add(self, item): self.items.append(item)

    def strarray(self):
        var = ["<?xml version=\"1.0\"?>\n",
               "<svg height=\"%f\" width=\"%f\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" >\n" % (self.height, self.width),
               " <g style=\"font-family:Lucida Console;font-weight:normal;fill-opacity:1;stroke:black;\" >\n"] #stroke-width:1;
        for item in self.items: var += item.strarray()            
        var += [" </g>\n</svg>\n"]
        return var

    def write_svg(self, filename=None):
        if filename:
            self.svgname = filename
        else:
            self.svgname = self.name + ".svg"
        file = open(self.svgname, 'w')
        file.writelines(self.strarray())
        file.close()
        return

class Line(object):
    def __init__(self, start, end, strokewidth=1):
        self.start = start #xy tuple
        self.end = end     #xy tuple
        self.strokewidth = strokewidth
        return

    def strarray(self):
        return ["  <line style=\"stroke-width:%s;\" x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" />\n" % \
                (str(self.strokewidth), self.start[0], self.start[1], self.end[0], self.end[1])]


class Circle(object):
    def __init__(self, center, radius, color):
        self.center = center #xy tuple
        self.radius = radius #xy tuple
        self.color = color   #rgb tuple in range(0,256)
        return

    def strarray(self):
        return ["  <circle cx=\"%f\" cy=\"%f\" r=\"%f\"\n" % \
                (self.center[0], self.center[1], self.radius),
                "    style=\"fill:%s;\"  />\n" % colorstr(self.color)]

class Rectangle(object):
    def __init__(self, origin, height, width, color):
        self.origin = origin
        self.height = height
        self.width = width
        self.color = color
        return

    def strarray(self):
        return ["  <rect x=\"%f\" y=\"%f\" height=\"%f\"\n" % \
                (self.origin[0], self.origin[1], self.height),
                "    width=\"%f\" style=\"fill:%s;\" />\n" % \
                (self.width, colorstr(self.color))]

class Text(object):
    def __init__(self, origin, text, size=24):
        self.origin = origin
        self.text = text
        self.size = size
        return

    def strarray(self):
        return ["  <text x=\"%f\" y=\"%f\" style=\"font-size:%s;stroke:none\">\n" % \
                (self.origin[0], self.origin[1], str(self.size)),
                "   %s\n" % self.text,
                "  </text>\n"]
    
def colorstr(rgb): return "#%x%x%x" % (rgb[0] / 16, rgb[1] / 16, rgb[2] / 16)
