/*
 * polar_speed table
 */
double polar_speed [30][15];
int wind_angle_polar[30];
double wind_speed_polar[15];

/*
 * double get_polar_speed (int wind_angle, double wind_speed)
 */
double
get_polar_speed (int wind_angle, double wind_speed)
{
	int x, y;
	double s, s1_1, s1_2, s2_1, s2_2, s1, s2;

	mlog_print (DLOG_DEBUG, LOG_TAG, "> get_polar_speed called for: %03d %.1f", wind_angle, wind_speed);

	if (wind_angle == 0 || wind_speed == 0.0)
		return 0.0;

	for (x=0; wind_angle > wind_angle_polar[x]; ++x);
	for (y=0; wind_speed > wind_speed_polar[y]; ++y);

	s1_1 = polar_speed[x-1][y-1];
	s1_2 = polar_speed[x][y-1];

	s2_1 = polar_speed[x-1][y];
	s2_2 = polar_speed[x][y];

	s1 = s1_1 + (s1_2 - s1_1) * (double)(wind_angle - wind_angle_polar[x-1]) / (double)(wind_angle_polar[x] - wind_angle_polar[x-1]);
	s2 = s2_1 + (s2_2 - s2_1) * (double)(wind_angle - wind_angle_polar[x-1]) / (double)(wind_angle_polar[x] - wind_angle_polar[x-1]);

	s = s1 + (s2 - s1) * (wind_speed - wind_speed_polar[y-1]) / (wind_speed_polar[y] - wind_speed_polar[y-1]);

	mlog_print (DLOG_DEBUG, LOG_TAG, "get_polar_speed: x=%d, y=%d", x, y);
	mlog_print (DLOG_DEBUG, LOG_TAG, "get_polar_speed: s1_1=%.1f, s1_2=%.1f", s1_1, s1_2);
	mlog_print (DLOG_DEBUG, LOG_TAG, "get_polar_speed: s2_1=%.1f, s2_2=%.1f", s2_1, s2_2);
	mlog_print (DLOG_DEBUG, LOG_TAG, "get_polar_speed: s1=%.1f, s2=%.1f", s1, s2);
	mlog_print (DLOG_DEBUG, LOG_TAG, "< get_polar_speed done: s=%.1f", s);

	return s;
}

/*
 * int load_polar_table ()
 */
int
load_polar_table ()
{
	char temp[256], *token, *str1, tmp1[16];
	char *delim = " \t\n\r";
	FILE *polar = NULL;
	int i, j, end_i, end_j;

	mlog_print (DLOG_INFO, LOG_TAG, "> load_polar_table called");

	snprintf (temp, 255,"%s/%s", MY_APP_DATA_PATH, POLAR_FILE);
	if ((polar = fopen(temp, "r")) == NULL) {
		mlog_print (DLOG_ERROR, LOG_TAG, "load_polar_table: could not open polar file <%s>", temp);
		return 0;
	}

	memset(temp, 0, sizeof(temp));
	/* first line is wind_angle_polar */
	if (fgets(temp, 255, polar) != NULL) {
		for (i = 0, str1 = temp; ; i++, str1 = NULL) {
			if (i >= 30)
				return 0;
			token = strtok(str1, delim);
			if (token == NULL)
				break;
			wind_angle_polar[i] = atoi(token);
		}
		wind_angle_polar[i] = 999;
		end_i = i;
	}

	/* and now the polar speed table */
	for (j=0; fgets(temp, 255, polar) != NULL; ++j) {
		if (j >= 15)
			return 0;
		for (i = 0, str1 = temp; ; i++, str1 = NULL) {
			if (i >= 30)
				return 0;
			token = strtok(str1, delim);
			if (token == NULL)
				break;
			/* first item is wind_speed_polar */
			if (i==0)
				wind_speed_polar[j] = atof(token);
			else
				polar_speed[i-1][j] = atof(token);
		}
		/* add one more item at the end */
		polar_speed[i-1][j] = polar_speed[i-2][j];
    }
	fclose (polar);

	/* add one more line */
	wind_speed_polar[j] = 99;
	for (i=0; i <= end_i; ++i)
		polar_speed[i][j] = polar_speed[i][j-1];
	end_j = j;

	mlog_print (DLOG_DEBUG, LOG_TAG, "<load_polar_table done: polar is %d x %d", end_i, end_j);
	memset(temp,0,sizeof(temp));
	for (i=0; i<=end_i; ++i) {
		sprintf (tmp1, "\t%d", wind_angle_polar[i]);
		strcat (temp, tmp1);
	}
	mlog_print (DLOG_DEBUG, LOG_TAG, "polar: %s", temp);
	for (j=0; j<=end_j; ++j) {
		memset(temp,0,sizeof(temp));
		for (i=0; i<=end_i; ++i) {
			if (i==0)
				sprintf (temp, "%.0f", wind_speed_polar[j]);
			sprintf (tmp1, "\t%.1f", polar_speed[i][j]);
			strcat (temp, tmp1);
		}
		mlog_print (DLOG_DEBUG, LOG_TAG, "polar: %s", temp);
	}
	return 1;
}
