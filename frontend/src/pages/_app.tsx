import '@primer/css/dist/primer.css';  
import { AnimatePresence } from 'motion/react';
import { AppProps } from 'next/app';
import Head from 'next/head';
import { ThemeProvider, BaseStyles } from '@primer/react';

export default function App({ Component, pageProps, router }: AppProps) {
    return (
        <ThemeProvider>
            <BaseStyles>
                <Head>
                    <title>Opton</title>
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <link rel="icon" href="/favicon.ico" />
                </Head>
                <AnimatePresence mode="wait">
                    <Component key={router.route} {...pageProps} />
                </AnimatePresence>
            </BaseStyles>
        </ThemeProvider>
    );
}
