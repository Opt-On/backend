import Link from "next/link";
import { Button } from "@primer/react";

export default function Home() {
    return (
        <main>
            <section>
                <div>
                    <h1>Home</h1>
                    <Link style={{ textDecoration: "none", color: "inherit" }} href="/transcript">Transcript Page</Link>
                    <Button variant="primary">Button</Button>
                </div>
            </section>
        </main>
    )
}