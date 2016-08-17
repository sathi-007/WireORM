package com.mast.android.orm.mocks;

import com.mast.android.orm.js2p.BatsmanNonStriker;
import com.mast.android.orm.js2p.BatsmanStriker;
import com.mast.android.orm.js2p.BowlerNonStriker;
import com.mast.android.orm.js2p.Cbz_comm;
import com.mast.android.orm.js2p.CommentaryList;
import com.mast.android.orm.js2p.MatchHeader;
import com.mast.android.orm.js2p.MatchTeamInfo;
import com.mast.android.orm.js2p.Miniscore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Created by sathish-n on 16/8/16.
 */

public class MockSingleton {

    private static MockSingleton mockSingleton = new MockSingleton();

    private MockSingleton(){

    }

    public static MockSingleton getInstance(){
        return mockSingleton;
    }


    public List<Cbz_comm> getCbzCommList(int count){
        List<Cbz_comm> cbz_commList = new ArrayList<>();
        for(int i=0;i<count;i++){
            cbz_commList.add(getMockCbzComm(i));
        }
        return cbz_commList;
    }

    private Cbz_comm getMockCbzComm(int index){
        Cbz_comm cbz_comm = new Cbz_comm();
        cbz_comm.setMiniscore(getMockMiniscore(index));
        cbz_comm.setMatchHeader(getMockMatchHeader(index));
        cbz_comm.setCommentaryList(getMockCommentaryList(20));
        return cbz_comm;
    }

    private Miniscore getMockMiniscore(int index){
        Miniscore miniscore = new Miniscore();
        miniscore.setCurrentRunRate(4.5);
        miniscore.setCustomStatus("no custom status");
        miniscore.setLastWicket("lastwicket "+index);
        miniscore.setBatsmanNonStriker(getMockBatsmanNonStriker(index));
        miniscore.setBatsmanStriker(getMockBatsmanStriker(index));
        miniscore.setBowlerNonStriker(getMockBowlerNonStriker(index));
        return miniscore;
    }

    private BatsmanNonStriker getMockBatsmanNonStriker(int index){
        BatsmanNonStriker batsmanNonStriker = new BatsmanNonStriker();
        batsmanNonStriker.setBatBalls(10);
        batsmanNonStriker.setBatDots(4);
        batsmanNonStriker.setBatFours(4);
        batsmanNonStriker.setBatId(index);
        batsmanNonStriker.setBatName("batsman "+index);
        batsmanNonStriker.setBatSixes(6);
        return batsmanNonStriker;
    }

    private BatsmanStriker getMockBatsmanStriker(int index){
        BatsmanStriker batsmanNonStriker = new BatsmanStriker();
        batsmanNonStriker.setBatBalls(10);
        batsmanNonStriker.setBatDots(4);
        batsmanNonStriker.setBatFours(4);
        batsmanNonStriker.setBatId(index);
        batsmanNonStriker.setBatName("batsman "+index);
        batsmanNonStriker.setBatSixes(6);
        return batsmanNonStriker;
    }

    private BowlerNonStriker getMockBowlerNonStriker(int index){
        BowlerNonStriker bowlerNonStriker = new BowlerNonStriker();
        bowlerNonStriker.setBowlEcon(34.5);
        bowlerNonStriker.setBowlId(index);
        bowlerNonStriker.setBowlMaidens(3);
        bowlerNonStriker.setBowlNoballs(2);
        bowlerNonStriker.setBowlRuns(34);
        return bowlerNonStriker;
    }

    private MatchHeader getMockMatchHeader(int index){
        MatchHeader matchHeader = new MatchHeader();
        matchHeader.setComplete(false);
        matchHeader.setDayNight(false);
        matchHeader.setDomestic(false);
        matchHeader.setMatchFormat("ODI");
        matchHeader.setMatchId(index);
        matchHeader.setComplete(false);
        matchHeader.setMatchType("international");
        List<String> players = new ArrayList<>();
        players.add("player1"+index);
        players.add("player2"+index);
        players.add("player3"+index);
        players.add("player4"+index);
        players.add("player5"+index);
        matchHeader.setPlayersOfTheMatch(players);
        matchHeader.setMatchTeamInfo(matchTeamInfoList());
        matchHeader.setStatus("live");
        return matchHeader;
    }
    private List<CommentaryList> getMockCommentaryList(int count){
        List<CommentaryList> commentaryList = new ArrayList<>();
        for(int i=0;i<count;i++){
            commentaryList.add(getMockCommentary(i));
        }
        return commentaryList;
    }

    private CommentaryList getMockCommentary(int index){
        CommentaryList commentaryList = new CommentaryList();
        commentaryList.setCommText("commText "+index);
        commentaryList.setEvent("No Event");
        commentaryList.setInningsId(index);
        return commentaryList;
    }

    private List<MatchTeamInfo> matchTeamInfoList(){
        List<MatchTeamInfo> matchTeamInfoList = new ArrayList<>();
        for(int i=0;i<2;i++){
            matchTeamInfoList.add(getMockMatchTeamInfo(i));
        }
        return matchTeamInfoList;
    }

    private MatchTeamInfo getMockMatchTeamInfo(int index){
        MatchTeamInfo matchTeamInfo = new MatchTeamInfo();
        matchTeamInfo.setBattingTeamId(index);
        matchTeamInfo.setBattingTeamShortName("battingTeam");
        matchTeamInfo.setBowlingTeamId(index+1);
        matchTeamInfo.setBowlingTeamShortName("bowlingTeam");
        return matchTeamInfo;
    }
}
