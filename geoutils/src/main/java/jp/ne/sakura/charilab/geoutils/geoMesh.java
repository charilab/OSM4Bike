package jp.ne.sakura.charilab.geoutils;

/*
MIT License

Copyright (c) 2017 Shunji Uno <uno@charilab.sakura.ne.jp>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

/*
 Libraries to convert between GSI geospatial mesh and latLng format.
 */
public class geoMesh {
    public int mesh1;
    public int mesh2;
    public int mesh3;
    public int mesh_class;

    public geoMesh(int m1, int m2, int m3, int cl) {
        mesh1 = m1;
        mesh2 = m2;
        mesh3 = m3;
        mesh_class = cl;
    }

    public geoMesh(double lat, double lng, int cl) {
        setAsLatLng(lat, lng, cl);
    }

    public geoMesh setAsLatLng(double lat, double lng, int cl) {
        int c1 = (int)(lat * 1.5);
        int c2 = (int)(lng - 100);
        int c3 = (int)(lat * 1.5 * 8) % 8;
        int c4 = (int)(lng * 8) % 8;
        int c5 = (int)(lat * 1.5 * 80) % 10;
        int c6 = (int)(lng * 80) % 10;

        mesh1 = c1*100+c2;
        mesh2 = c3*10+c4;
        mesh3 = c5*10+c6;
        mesh_class = cl;
        return this;
    }

    public geoMesh[] getNeighbours() {
        geoMesh[] neighbours = new geoMesh[8];
        int n1, n2, n3, s1, s2, s3;
        int e1, e2, e3, w1, w2, w3;

        int y1 = mesh1 / 100;
        int x1 = mesh1 % 100;
        int y2 = mesh2 / 10;
        int x2 = mesh2 % 10;
        int y3 = mesh3 / 10;
        int x3 = mesh3 % 10;
        switch (mesh_class) {
            case 1:
                n1 = y1 + 1; s1 = y1 - 1; e1 = x1 + 1; w1 = x1 - 1;
                n2 = s2 = e2 = w2 = 0;
                n3 = s3 = e3 = w3 = 0;
                break;
            case 2:
                n1 = y1; s1 = y1; e1 = x1; w1 = x1;
                n2 = y2 + 1; s2 = y2 - 1; e2 = x2 + 1; w2 = x2 - 1;
                n3 = s3 = e3 = w3 = 0;
                break;
            case 3:
                n1 = y1; s1 = y1; e1 = x1; w1 = x1;
                n2 = y2; s2 = y2; e2 = x2; w2 = x2;
                n3 = y3 + 1; s3 = y3 - 1; e3 = x3 + 1; w3 = x3 - 1;
                break;
            default:
                return null;
        }

        // north adjustment
        if (n3 > 9) { n3 = 0; n2 = y2 + 1;}
        if (n2 > 7) { n2 = 0; n1 = y1 + 1; }
        // south adjustment
        if (s3 < 0) { s3 = 9; s2 = y2 - 1; }
        if (s2 < 0) { s2 = 7; s1 = y1 - 1; }
        // east adjustment
        if (e3 > 9) { e3 = 0; e2 = x2 + 1; }
        if (e2 > 7) { e2 = 0; e1 = x1 + 1; }
        // west adjustment
        if (w3 < 0) { w3 = 9; w2 = x2 - 1; }
        if (w2 < 0) { w2 = 7; w1 = x1 - 1; }

        neighbours[0] = new geoMesh(n1*100 + w1, n2*10 + w2, n3*10 + w3, mesh_class);
        neighbours[1] = new geoMesh(n1*100 + x1, n2*10 + y2, n3*10 + y3, mesh_class);
        neighbours[2] = new geoMesh(n1*100 + e1, n2*10 + e2, n3*10 + e3, mesh_class);
        neighbours[3] = new geoMesh(y1*100 + w1, y2*10 + w2, y3*10 + w3, mesh_class);
        neighbours[4] = new geoMesh(y1*100 + e1, y2*10 + e2, y3*10 + e3, mesh_class);
        neighbours[5] = new geoMesh(s1*100 + w1, s2*10 + w2, s3*10 + w3, mesh_class);
        neighbours[6] = new geoMesh(s1*100 + x1, s2*10 + y2, s3*10 + y3, mesh_class);
        neighbours[7] = new geoMesh(s1*100 + e1, s2*10 + e2, s3*10 + e3, mesh_class);
        return neighbours;
    }

    // order minLat, minLng, maxLat, maxLng
    public double[] toLatLng() {
        double area[] = new double[4];

        int y1 = mesh1 / 100;
        int x1 = mesh1 % 100;
        int y2 = mesh2 / 10;
        int x2 = mesh2 % 10;
        int y3 = mesh3 / 10;
        int x3 = mesh3 % 10;

        double lat1 = y1 / 1.5;
        double lng1 = x1 + 100.0;
        double lat2 = y2 * 5.0;
        double lng2 = x2 * 7.5;
        double lat3 = y3 * 30.0;
        double lng3 = x3 * 45.0;
        switch(mesh_class) {
            case 1:
                area[0] = lat1;
                area[1] = lng1;
                area[2] = area[0]+(1.0/1.5);
                area[3] = area[1]+1.0;
                break;

            case 2:
                area[0] = lat1+(lat2/60);
                area[1] = lng1+(lng2/60);
                area[2] = area[0]+(5.0/60.0);
                area[3] = area[1]+(7.5/60.0);
                break;

            case 3:
                area[0] = lat1+(lat2/60)+(lat3/3600);
                area[1] = lng1+(lng2/60)+(lng3/3600);
                area[2] = area[0]+(30.0/3600.0);
                area[3] = area[1]+(45.0/3600.0);
        }
        return area;
    }

    // order minLat, minLng, maxLat, maxLng
    public double[] toLatLngWithNeibours() {
        double area[] = toLatLng();

        switch(mesh_class) {
            case 1:
                area[0] = area[0]-(1.0/1.5);
                area[1] = area[1]-1.0;
                area[2] = area[2]+(1.0/1.5);
                area[3] = area[3]+1.0;
                break;

            case 2:
                area[0] = area[0]-(5.0/60.0);
                area[1] = area[1]-(7.5/60.0);
                area[2] = area[2]+(5.0/60.0);
                area[3] = area[3]+(7.5/60.0);
                break;

            case 3:
                area[0] = area[0]-(30.0/3600.0);
                area[1] = area[1]-(45.0/3600.0);
                area[2] = area[2]+(30.0/3600.0);
                area[3] = area[3]+(45.0/3600.0);
        }
        return area;
    }

    public boolean nearby(double lat, double lng) {
        double[] base_area = toLatLng();
        double[] area = new double[4];
        switch (mesh_class) {
            case 1:
                area[0] = base_area[0]-(0.5/1.5);
                area[1] = base_area[1]-(0.5);
                area[2] = base_area[2]+(0.5/1.5);
                area[3] = base_area[3]+(0.5);
                break;
            case 2:
                area[0] = base_area[0]-(5.0/60.0/2);
                area[1] = base_area[1]-(7.5/60.0/2);
                area[2] = base_area[2]+(5.0/60.0/2);
                area[3] = base_area[3]+(7.5/60.0/2);
                break;
            case 3:
                area[0] = base_area[0]-(30.0/3600.0/2);
                area[1] = base_area[1]-(45.0/3600.0/2);
                area[2] = base_area[2]+(30.0/3600.0/2);
                area[3] = base_area[3]+(45.0/3600.0/2);
                break;
        }
        if ((area[0]<lat) && (area[1]<lng) && (area[2]>lat) && (area[3]>lng)) {
            return true;
        }
        return false;
    }

    public boolean equals(geoMesh that) {
        return ((this.mesh1 == that.mesh1)
                &&(this.mesh2 == that.mesh2)
                &&(this.mesh3 == that.mesh3)
                &&(this.mesh_class == that.mesh_class));
    }
}
