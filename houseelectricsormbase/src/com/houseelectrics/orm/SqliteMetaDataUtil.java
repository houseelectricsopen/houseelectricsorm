package com.houseelectrics.orm;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by roberttodd on 21/06/2015.
 */
public class SqliteMetaDataUtil
{
    public static class TableMetaData
    {
        public String name;
        public static class ColumnMetaData
        {
            public String type;
            public String name;
        }
        public Map<String, ColumnMetaData> name2Column;
    }

    /**
     * process the result of
     * SELECT sql FROM sqlite_master WHERE tbl_name=${tablename}
     * @param strInfo e.g. CREATE TABLE Root(   FieldB INTEGER,   FieldA TEXT)
     * @return
     */
    public TableMetaData extractTableMetadataFromSqliteMasterSql(String strInfo) throws ParseException
    {
        TableMetaData tableMetaData = new TableMetaData();
        tableMetaData.name2Column = new HashMap<String, TableMetaData.ColumnMetaData>();
        strInfo = strInfo.trim();
        int tableCommandIndex = strInfo.indexOf("TABLE");
        if (-1==tableCommandIndex) tableCommandIndex = strInfo.indexOf("table");
        if (-1==tableCommandIndex)
        {
            throw new ParseException("cant find table command", 0);
        }
        tableCommandIndex+=5;
        int openBracketIndex = strInfo.indexOf('(', tableCommandIndex);
        if (openBracketIndex==-1)
        {
            throw new ParseException("cant find ( in  " + strInfo, 0);
        }
        int closeBracketIndex = strInfo.indexOf(')', openBracketIndex+1);
        if (closeBracketIndex==-1)
        {
            throw new ParseException("cant find ) in  " + strInfo, openBracketIndex);
        }
        tableMetaData.name = strInfo.substring(tableCommandIndex, openBracketIndex).trim();
        String strCols = strInfo.substring(openBracketIndex+1, closeBracketIndex);
        String strColumns[] = strCols.split(",");
        for (String strColumn : strColumns)
        {
            StringTokenizer stk  = new StringTokenizer(strColumn, " ", false);
            if (stk.countTokens()<2) throw new ParseException("cant find name and fieldName in column spec " + strColumn, closeBracketIndex);
            TableMetaData.ColumnMetaData cmd = new TableMetaData.ColumnMetaData();
            cmd.name=stk.nextToken().trim();
            cmd.type=stk.nextToken().trim();
            tableMetaData.name2Column.put(cmd.name, cmd);
        }

        return tableMetaData;
    }

    public TableMetaData extractTableMetadataNullIfNotExists(SqliteDatabaseService sqliteDatabaseService, String tablename) throws Exception
    {
        List<Object> strInfos =  sqliteDatabaseService.excecuteRawQuerySingleColumn("SELECT sql FROM sqlite_master WHERE tbl_name='" + tablename.trim() + "'");
        if (strInfos == null || strInfos.size()==0)
        {
            return null;
        }
        TableMetaData tmd = extractTableMetadataFromSqliteMasterSql(strInfos.get(0).toString());
        return tmd;
    }

}
