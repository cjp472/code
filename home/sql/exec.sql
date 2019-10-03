create FUNCTION [dbo].[toMap]
(
@SplitString nvarchar(max),  --源字符串
@Separator nvarchar(10)=' ',  --分隔符号，默认为空格
@Separator1 	nvarchar(10)='=' --分割kv ,默认=
)
RETURNS @SplitStringsTable TABLE  --输出的数据表
(
[key] nvarchar(max),
[val] nvarchar(max)
)
AS
BEGIN
DECLARE @CurrentIndex int;
DECLARE @NextIndex int;
DECLARE @ReturnText nvarchar(max);
DECLARE @dy int;

SELECT @CurrentIndex=1;
WHILE(len(@SplitString)>=@CurrentIndex)
BEGIN
SELECT @NextIndex=charindex(@Separator,@SplitString,@CurrentIndex);
IF(@NextIndex=0 OR @NextIndex IS NULL)
SELECT @NextIndex=len(@SplitString)+LEN(@Separator);
SELECT @ReturnText=substring(@SplitString,@CurrentIndex,@NextIndex-@CurrentIndex);--拆分
SELECT @dy=charindex(@Separator1,@ReturnText);--用来拆分=
INSERT INTO @SplitStringsTable([key],[val]) VALUES(
case when @dy =0 then @ReturnText else SUBSTRING(@ReturnText, 1, @dy-1) end
,case when @dy =0 then '' else SUBSTRING(@ReturnText,@dy+LEN(@Separator1),len(@ReturnText)+1-@dy-LEN(@Separator1)) end);
SELECT @CurrentIndex=@NextIndex+LEN(@Separator);
END
RETURN;
END
GO

create FUNCTION [dbo].[getField]
(
@table nvarchar(max),  --表名
@parm nvarchar(max)
)
RETURNS @fieldTable TABLE  --输出的数据表
(
[key] nvarchar(max),
[val] nvarchar(max),
[type] nvarchar(max)
)
AS
BEGIN
insert into @fieldTable ([key],[val],[type])
		SELECT
		a.name
		,map.val
		,case when b.name in('bigint' ,'decimal','money','smallmoney','float','int','numeric','real','smallint','tinyint') then 'num'
		when b.name in('datetime','time','smalldatetime','date','datetime2') then 'date'
		when b.name in('uniqueidentifier') then 'id'
		else 'string' end type

		FROM  syscolumns a
		left join systypes b on a.xtype=b.xusertype
		inner join sysobjects d on a.id=d.id and d.xtype='U' and d.name = @table
		join (select * from dbo.toMap(@parm,'@,','@='))map on map.[key] = a.name
		where b.name is not null
union ALL
select [key],[val],'string' from dbo.toMap(@parm,'@,','@=') where [key] = '@1'
UNION ALL
	SELECT
		a.name
		,'20'
		,case when b.name in('bigint' ,'decimal','money','smallmoney','float','int','numeric','real','smallint','tinyint') then 'num'
		when b.name in('datetime','time','smalldatetime','date','datetime2') then 'date'
		when b.name in('uniqueidentifier') then 'id'
		else 'string' end type

		FROM  syscolumns a
		left join systypes b on a.xtype=b.xusertype
		inner join sysobjects d on a.id=d.id and d.xtype='U' and d.name = @table
		where b.name is not null and len(@parm) = 0
RETURN;
END
GO
create FUNCTION [dbo].[getSqlstr]
	(
	 @table VARCHAR(100) , @parm VARCHAR(max),@where VARCHAR(max),@order VARCHAR(max),@sqlType VARCHAR(100)
	)
	RETURNS @sqlTable TABLE  --输出的数据表
	(
	[sql] nvarchar(max)
	)
	AS
	BEGIN
	DECLARE @sql1  VARCHAR(max)='',@sql2  VARCHAR(max)='',@sql  VARCHAR(max)='',@page varchar(max) , @limit varchar(max),@orderStr varchar(max), @Count INT = 0

	if (@sqlType = 'insert')
	begin
			select @sql1+=','+[key]
			, @sql2+=','+
							case
									when type='num' then [val]
									when type ='date' and [val]='1' then 'GETDATE()'
									when type ='id' and [val]='1' then 'NEWID()'
									else  ''''+[val]+''''
							end
			from [dbo].[getField](@table,@parm)
			if(LEN(@sql1)>1)set @sql1 = SUBSTRING(@sql1,2,LEN(@sql1)-1)
			if(LEN(@sql2)>1)set @sql2 = SUBSTRING(@sql2,2,LEN(@sql2)-1)
			set @sql = 'insert into ' + @table +' ( '+@sql1+' ) values ( ' + @sql2 + ')'
	end
	else if(@sqlType = 'update')										--update语句
	begin
			select @sql1+=','+[key]+'='+									--获取set
							case
									when type='num' then [val]
									when type ='date' and [val]='1' then 'GETDATE()'
									when type ='id' and [val]='1' then 'NEWID()'
									when type ='id' and [val]='' then 'null'
									else  ''''+[val]+''''
							end
			from [dbo].[getField](@table,@parm)
			select @sql2+=' and '+
							case [key]
									when '@1' then [val]
							else
							[key]+'='+									--获取where
									case
											when type='num' then [val]
											else  ''''+[val]+''''
									end
							end
			from (select a.[key],a.[val],b.[type] from [dbo].[toMap](@where,'@,','@=')a  join [dbo].[getField](@table,@where)b ON a.[key] = b.[key])c
			if(LEN(@sql1)>1)set @sql1 = SUBSTRING(@sql1,2,LEN(@sql1)-1)		--拼接set语句
			if(LEN(@sql2)>1)set @sql2 = SUBSTRING(@sql2,5,LEN(@sql2)-4)		--拼接where条件
			set @sql = 'update ' + @table +' set '+@sql1+' where ' + @sql2	--拼接update语句
			if(1>LEN(@sql2)) set @sql = 'select '' '' sql ,''@0'' sqlStatus , ''暂不支持where条件为空的更新语句'' msg '
	end
	else if(@sqlType = 'delete')										--update语句
	begin
			select @sql2+=' and '+
							case [key]
									when '@1' then [val]
							else
							[key]+'='+									--获取where
									case
											when type='num' then [val]
											else  ''''+[val]+''''
									end
							end
			from (select a.[key],a.[val],b.[type] from [dbo].[toMap](@where,'@,','@=')a  join [dbo].[getField](@table,@where)b ON a.[key] = b.[key])c
			if(LEN(@sql1)>1)set @sql1 = SUBSTRING(@sql1,2,LEN(@sql1)-1)		--拼接set语句
			if(LEN(@sql2)>1)set @sql2 = SUBSTRING(@sql2,5,LEN(@sql2)-4)		--拼接where条件
			set @sql = 'delete from ' + @table +' where ' + @sql2	--拼接delete语句
			if(1>LEN(@sql2)) set @sql = 'select '' '' sql ,''@0'' sqlStatus , ''暂不支持where条件为空的删表语句'' msg '
	end
	else if(@sqlType = 'select')										--查询语句
	begin
			select @sql1+=','+												--是否查询指定字段
							case
											when [key]='@1' then [val]
											when type ='date'and (select PATINDEX('%[^0-9]%', [val]) )= 0 then 'convert(varchar ,'+[key]+','+[val]+')' + [key]
											else  [key] +' ' + case when (select PATINDEX('%[^0-9]%', [val]) )= 0 then [key] else [val] end
							end
			 from [dbo].[getField](@table,@parm)
			select @sql2+=' and '+
									case [key]
											when '@1' then [val]
									else
									[key]+'='+									--获取where
											case
													when type='num' then [val]
													else  ''''+[val]+''''
											end
									end
			from (select a.[key],a.[val],b.[type] from [dbo].[toMap](@where,'@,','@=')a  join [dbo].[getField](@table,@where)b ON a.[key] = b.[key])c
			if(LEN(@sql1)>1)set @sql1 = SUBSTRING(@sql1,2,LEN(@sql1)-1)		--拼接set语句
			else set @sql1 = '*'
			if(LEN(@sql2)>1)set @sql2 = SUBSTRING(@sql2,5,LEN(@sql2)-4)		--拼接where条件
			set @sql = 'select ' +@sql1 +' from '+ @table
			if(LEN(@sql2)>1) set @sql += ' where ' + @sql2
	end
	else if(@sqlType = 'count')										--查询语句
	begin
			select @sql2+=' and '+
									case [key]
											when '@1' then [val]
									else
									[key]+'='+									--获取where
											case
													when type='num' then [val]
													else  ''''+[val]+''''
											end
									end
			from (select a.[key],a.[val],b.[type] from [dbo].[toMap](@where,'@,','@=')a  join [dbo].[getField](@table,@where)b ON a.[key] = b.[key])c
			if(LEN(@sql1)>1)set @sql1 = SUBSTRING(@sql1,2,LEN(@sql1)-1)		--拼接set语句
			else set @sql1 = '*'
			if(LEN(@sql2)>1)set @sql2 = SUBSTRING(@sql2,5,LEN(@sql2)-4)		--拼接where条件
			set @sql = 'select count(1) count from '+ @table
			if(LEN(@sql2)>1) set @sql += ' where ' + @sql2
	end
	else if(@sqlType = 'page')										--查询语句
	begin

			select @orderStr=[val] from dbo.toMap(@order,',','=') where [key] = 'order'
			select @page=[val] from dbo.toMap(@order,',','=') where [key] = 'page'
			select @limit=[val] from dbo.toMap(@order,',','=') where [key] = 'limit'
			select @sql1+=','+												--是否查询指定字段
							case
											when [key]='@1' then [val]
											when type ='date'and (select PATINDEX('%[^0-9]%', [val]) )= 0 then 'convert(varchar ,'+[key]+','+[val]+')' + [key]
											else  [key] +' ' + case when (select PATINDEX('%[^0-9]%', [val]) )= 0 then [key] else [val] end
							end
			 from [dbo].[getField](@table,@parm)
			select @sql2+=' and '+
									case [key]
											when '@1' then [val]
									else
									[key]+'='+									--获取where
											case
													when type='num' then [val]
													else  ''''+[val]+''''
											end
									end
			from (select a.[key],a.[val],b.[type] from [dbo].[toMap](@where,'@,','@=')a  join [dbo].[getField](@table,@where)b ON a.[key] = b.[key])c
			if(LEN(@sql1)>1)set @sql1 = SUBSTRING(@sql1,2,LEN(@sql1)-1)		--拼接set语句
			else set @sql1 = '*'
			if(LEN(@sql2)>1)set @sql2 = SUBSTRING(@sql2,5,LEN(@sql2)-4)		--拼接where条件
			set @sql ='select top '+@limit+' *
			from (select row_number()
			over(order by '+@orderStr+') as rownumber,' +@sql1 +' from '+ @table

			if(LEN(@sql2)>1) set @sql += ' where ' + @sql2

			set @sql+=') temp_row
			where rownumber>(('+@page+'-1)*'+@limit+')'

	end

	insert into @sqlTable ([sql]) values (@sql)

	RETURN;
	END
GO
create PROCEDURE [dbo].[selects]
		@table varchar(max),
		@parm varchar(max),
		@where varchar(max),
		@order varchar(max),
		@sqlType varchar(max)
AS
	DECLARE @sql VARCHAR(max)=''
        begin try
            select @sql = sql from [dbo].[getSqlstr](@table,@parm,@where,@order, @sqlType)
            print @sql

						if(@sqlType is not null and @sqlType in('insert','update','delete','auto'))
						begin
								exec(@sql)
                select  @@rowcount num , @sql sql
						end
						ELSE
						BEGIN
							exec(@sql)
						END
        end try
        begin catch
            select @sql sql ,'@0' sqlStatus , ERROR_MESSAGE() msg
        end catch
GO
create PROCEDURE autoInsertORUpdate
		@table varchar(max),
		@parm varchar(max),
		@where varchar(max)
AS


DECLARE @sql nvarchar(max)='', @Count INT = 0
begin try
		select @sql = sql from [dbo].[getSqlstr](@table,'',@where,'', 'select')
		set @sql = 'select @RowNum  = count(1) from (' +@sql +')a'
		print @sql
		EXEC sp_executesql @sql,N'@RowNum INT OUTPUT',@Count OUTPUT
		IF (@Count>0 and len(@where)>0)
				begin
						select @sql = sql from [dbo].[getSqlstr](@table,@parm,@where,'', 'update')
						print(@sql)
						exec (@sql)
						select  @@rowcount num , @sql sql
				end
		ELSE
				begin
						select @sql = sql from [dbo].[getSqlstr](@table,@parm,@where,'', 'insert')
						print(@sql)
						exec (@sql)
						select  @@rowcount num , @sql sql
				end
end try
begin catch
				select @sql sql ,'@0' sqlStatus , ERROR_MESSAGE() msg
end catch

