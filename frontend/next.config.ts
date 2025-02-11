import type { NextConfig } from "next";
import withTM from "next-transpile-modules";

const withTranspileModules = withTM(["@primer/react"]);

const nextConfig: NextConfig = {
  reactStrictMode: true,
};

export default withTranspileModules(nextConfig);