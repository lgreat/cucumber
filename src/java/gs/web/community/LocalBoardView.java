package gs.web.community;

import gs.data.community.Discussion;
import gs.data.community.DiscussionReply;
import gs.data.community.User;
import gs.data.community.local.LocalBoard;

import java.util.List;
import java.util.Date;

/**
 * Access to a local board and extra data for the view.
 *
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class LocalBoardView {
    private LocalBoard _localBoard;
    private String _boardUrl;

    public LocalBoardView(LocalBoard localBoard, String boardUrl) {
        _localBoard = localBoard;
        _boardUrl = boardUrl;
    }

    public LocalBoard getLocalBoard() {
        return _localBoard;
    }

    public void setLocalBoard(LocalBoard localBoard) {
        _localBoard = localBoard;
    }

    public String getBoardUrl() {
        return _boardUrl;
    }

    public void setBoardUrl(String boardUrl) {
        _boardUrl = boardUrl;
    }
}