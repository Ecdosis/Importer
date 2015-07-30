/*
 * This file is part of Importer.
 *
 *  Importer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Importer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Importer.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2015
 */
package importer.handler.post.stages;

/**
 * An image file
 * @author desmond
 */
public class ImageFile
{
    String type;
    String name;
    byte[] data;
    public ImageFile( String name, String type, byte[] data )
    {
        this.data = data;
        this.name = name;
        this.type = type;
    }
    public byte[] getData()
    {
        return this.data;
    }
    public String getName()
    {
        return this.name;
    }
    public String getType()
    {
        return this.type;
    }
}
