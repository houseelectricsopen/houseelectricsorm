package com.houseelectrics.android.houseelectricsormandroidtest.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.dexmaker.stock.ProxyBuilder;
import com.houseelectrics.orm.ProxyFactory;
import com.houseelectrics.orm.SqliteDatabaseService;
import com.houseelectrics.orm.android.AndroidDbUtil;
import com.houseelectrics.orm.android.SqliteDatabaseAndroidService;
import com.houseelectrics.orm.test.Asserter;
import com.houseelectrics.orm.test.CrudTest;
import com.houseelectrics.orm.test.LazyLoadingTest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class MainActivity extends Activity
{
    TextView messageText;
    String dbAbsolutePath =null;
    AndroidDbUtil androidDbUtil = new AndroidDbUtil();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbAbsolutePath = androidDbUtil.getOrCreateExternalStorageDbPath("ormtestx", "ormtestx");

        Button button = (Button) findViewById(R.id.testButton);
        Button.OnClickListener bl = new Button.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                runTest(dbAbsolutePath);
            }
        };
        button.setOnClickListener(bl);

        messageText = (TextView) findViewById(R.id.messageText);

    }

    SqliteDatabaseAndroidService createDb(String dbAbsolutePath)
    {
        SqliteDatabaseAndroidService da = androidDbUtil.createDb(this, dbAbsolutePath, true);
        da.setLogger(new SqliteDatabaseService.Logger()
        {
            @Override
            public void info(String message)
            {
                android.util.Log.i(MainActivity.class.getName(), message);
            }

            @Override
            public void error(String message)
            {
                android.util.Log.e(MainActivity.class.getName(), message);
                RuntimeException rex = new RuntimeException(message);
                rex.fillInStackTrace();
                throw rex;
            }

            @Override
            public void error(String message, Throwable error)
            {
                android.util.Log.e(MainActivity.class.getName(), message, error);
                RuntimeException rex = new RuntimeException(message, error);
                throw rex;
            }

            @Override
            public void sql(String sql)
            {
                android.util.Log.i(MainActivity.class.getName(), sql);
            }
        });
        return da;
    }

    void runTest(String dbAbsolutePath)
    {
        CrudTest crudTest = new CrudTest();
        LazyLoadingTest lazyLoadingTest = new LazyLoadingTest();
        Asserter asserter = new Asserter()
        {
            boolean testEqual(Object o, Object o2)
            {
                return !(( o==null && o2==null) || (o!=null && o.equals(o2)));
            }

            @Override
            public void assertEqual(Object o, Object o2)
            {
                if (testEqual( o, o2)) throw new RuntimeException("failed expected, actual " + o + "," + o2);
            }

            @Override
            public void assertEqual(String s, Object o, Object o2)
            {
                if (testEqual( o, o2)) throw new RuntimeException("failed " + s +" expected, actual " + o + "," + o2) ;
            }
        };
        SqliteDatabaseAndroidService da;
        int testCount=0;
        try
        {
            long time = System.currentTimeMillis();

            messageText.setText( " tests Starting starting !");

            AndroidOrmExamples androidOrmExamples = new AndroidOrmExamples();
            androidOrmExamples.testBasicCrud(this , asserter);
            testCount++;
            messageText.setText( " testLazyLoading Success !");

            da = createDb(dbAbsolutePath);
            File dexCacheFile=null;
            dexCacheFile = getApplicationContext().getDir("dexProxyClasses", Context.MODE_MULTI_PROCESS);

            lazyLoadingTest.testLazyLoading(asserter, da, (new DexProxyFactory()).createProxyFactory(dexCacheFile));
            testCount++;
            messageText.setText( " testLazyLoading Success !");

            da = createDb(dbAbsolutePath);
            crudTest.crudTestSimpleFromEmptyDb(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " crudTestSimpleFromEmptyDb Success !");

            da = createDb(dbAbsolutePath);
            testCount++;
            crudTest.crudWithDetailTestFromEmptyDb(asserter, da);
            messageText.setText(messageText.getText() + " crudWithDetailTestFromEmptyDb Success !");

            da = createDb(dbAbsolutePath);
            crudTest.crudWithListProperty(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " crudWithListProperty Success !");

            da = createDb(dbAbsolutePath);
            crudTest.testRawSelect(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testRawSelectDeep Success !");
            da = createDb(dbAbsolutePath);
            crudTest.testRawSelectDeep(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testRawSelect Success !");
            da = createDb(dbAbsolutePath);
            crudTest.testRawSelectSingleColumn(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testRawSelectSingleColumn Success !");
            da = createDb(dbAbsolutePath);
            crudTest.testSubClass(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testSubClass Success !");
            da = createDb(dbAbsolutePath);
            crudTest.testPropertyPersistenceFilter(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testPropertyPersistenceFilter Success !");
            da.addHighPriorityMapping(createStringArrayMapping());
            da.addHighPriorityMapping(createStringListMapping());
            da.addHighPriorityMapping(createIntListMapping());
            crudTest.testCustomTypeMappings(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testCustomTypeMappings !");
            da = createDb(dbAbsolutePath);
            crudTest.testDeepLoadWithEmptyDetail(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testDeepLoadWithEmptyDetail !");
            da = createDb(dbAbsolutePath);
            crudTest.testRepeatedShallowLoad(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testRepeatedShallowLoad !");
            da = createDb(dbAbsolutePath);
            crudTest.testDeepLoadAlreadyPopulated(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testDeepLoadAlreadyPopulated !");
            da = createDb(dbAbsolutePath);
            crudTest.testMultipleRoots(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testMultipleRoots !");
            da = createDb(dbAbsolutePath);
            crudTest.testPolymorphicReference(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testPolymorphicReference !");
            da = createDb(dbAbsolutePath);
            crudTest.testPolymorphicListReferences(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testPolymorphicNullReference !");
            da = createDb(dbAbsolutePath);
            crudTest.testPolymorphicNullReference(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testPolymorphicListReferences !");
            da = createDb(dbAbsolutePath);
            crudTest.testPolymorphicStackReferences(asserter, da);
            testCount++;
            messageText.setText(messageText.getText() + " testPolymorphicStackReferences !");

           // dexTest(asserter);
            testCount++;
            messageText.setText(messageText.getText() + " dexTest !");


            time = System.currentTimeMillis()-time;
            messageText.setText(messageText.getText() + "  completed " + testCount + " tests in " + time + "ms");

        }
        catch (Exception ex)
        {
            android.util.Log.e(getClass().getName(), ex.getMessage(), ex);
            messageText.setText("failed on " + ex.getMessage());
        }
    }

    @Override
    protected void onPause() {
        androidDbUtil.close();
        super.onPause();
    }

    SqliteDatabaseAndroidService.AndroidSqliteTypeConverter createIntListMapping()
    {
        SqliteDatabaseAndroidService.AndroidSqliteTypeConverter converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {

            @Override
            public void write(ContentValues contentValues, String propertyName, Object value)
            {
                List<Integer> l =  (List<Integer>)value;
                StringBuffer sb = new StringBuffer();
                for (int done=0; done<l.size(); done++)
                {
                    if (done!=0) sb.append(',');
                    sb.append(l.get(done));
                }
                contentValues.put(propertyName, sb.toString());
            }

            @Override
            public Object read(Cursor rs, int zeroIndex)
            {
                String str = rs.getString(zeroIndex);
                if (str==null) {return null;}
                String []strs = str.split(",");
                List<Integer> result = new ArrayList<Integer>();
                for (int sd=0; sd<strs.length; result.add( Integer.parseInt(strs[sd])), sd++) {}
                return result;
            }
            @Override
            public String getSqlitePropertyType()
            {
                return "TEXT";
            }

            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)
            {
                return type == List.class && genericTypeParamers!=null && genericTypeParamers.length>0 && genericTypeParamers[0]==Integer.class;
            }
        };

        return converter;
    }

    SqliteDatabaseAndroidService.AndroidSqliteTypeConverter createStringArrayMapping()
    {
        SqliteDatabaseAndroidService.AndroidSqliteTypeConverter converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override
            public void write(ContentValues contentValues, String propertyName, Object value)
            {
                StringBuffer sb = new StringBuffer();
                String[] strs = (String[]) value;
                for (int done=0; done<strs.length; done++)
                {
                    if (done>0) sb.append(",");
                    sb.append(strs[done]);
                }
                contentValues.put(propertyName, sb.toString());
            }

            @Override
            public Object read(Cursor rs, int zeroIndex)
            {
                String str = rs.getString(zeroIndex);
                return str==null?null:str.split(",");
            }

            @Override
            public String getSqlitePropertyType()
            {
                return "TEXT";
            }

            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)
            {
                return type == String[].class;
            }
        };
        return converter;
    }

    SqliteDatabaseAndroidService.AndroidSqliteTypeConverter createStringListMapping()
    {
        SqliteDatabaseAndroidService.AndroidSqliteTypeConverter
                converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override
            public void write(ContentValues contentValues, String propertyName, Object value)
            {
                List<String> l =  (List<String>)value;
                StringBuffer sb = new StringBuffer();
                for (int done=0; done<l.size(); done++)
                {
                    if (done!=0) sb.append(',');
                    sb.append(l.get(done));
                }
                contentValues.put(propertyName, sb.toString());
            }

            @Override
            public Object read(Cursor rs, int zeroIndex)
            {
                String str = rs.getString(zeroIndex);
                if (str==null) {return null;}
                String []strs = str.split(",");
                List<String> result = new ArrayList<String>();
                for (int sd=0; sd<strs.length; result.add(strs[sd]), sd++) {}
                return result;
            }

            @Override
            public String getSqlitePropertyType()  {  return "TEXT"; }

            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)
            {
                return  type==List.class && genericTypeParamers!=null
                        && genericTypeParamers.length>0 && genericTypeParamers[0]==String.class;
            }
        };
        return converter;
    }



/*
public void dexTest(Asserter asserter) throws IOException
{
    InvocationHandler handler = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("nextInt")) {
                // Chosen by fair dice roll, guaranteed to be random.
                return 4;
            }
            Object result = ProxyBuilder.callSuper(proxy, method, args);
            System.out.println("Method: " + method.getName() + " args: "
                    + Arrays.toString(args) + " result: " + result);
            return result;
        }
    };
    File dexCacheFile=null;
    //dexCacheFile =  getInstrumentation().getTargetContext().getDir("dx", Context.MODE_PRIVATE)
    dexCacheFile = getApplicationContext().getDir("dx2", Context.MODE_MULTI_PROCESS);
    String fullPath = dexCacheFile.getAbsolutePath();
    fullPath = dexCacheFile.getCanonicalPath();

    Random debugRandom = ProxyBuilder.forClass(Random.class)
            .dexCache(dexCacheFile)
            .handler(handler)
            .build();
    asserter.assertEqual(4, debugRandom.nextInt());
    debugRandom.setSeed(0);
    asserter.assertEqual(debugRandom.nextBoolean(), true);
}
*/


}
