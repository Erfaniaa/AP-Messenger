package sample;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by erfan on 6/20/16.
 */

class Chat
{
    public User src;
    public String dstID;
    public boolean isGroup;
    public Chat(User _src, String _dstID, boolean _isGroup)
    {
        src = _src;
        dstID = _dstID;
        isGroup = _isGroup;
    }
}