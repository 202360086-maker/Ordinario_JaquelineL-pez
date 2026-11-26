import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = 'https://xuyqfnwkcupwhucnvkhy.supabase.co';
const SUPABASE_ANON_KEY = 'sb_publishable_gFrtSIOrA44liKTGtJWXXQ_NcvMw684';

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);